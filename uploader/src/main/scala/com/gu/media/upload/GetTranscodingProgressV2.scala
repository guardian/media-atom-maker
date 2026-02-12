package com.gu.media.upload

import software.amazon.awssdk.services.mediaconvert.model.{
  ContainerType,
  GetJobRequest,
  Job,
  JobStatus
}
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.{
  ImageAssetDimensions,
  SelfHostedAsset,
  VideoAsset,
  VideoSource
}
import com.gu.media.upload.model.{SelfHostedUploadMetadata, Upload}

import scala.jdk.CollectionConverters._

class GetTranscodingProgressV2
    extends LambdaWithParams[Upload, Upload]
    with MediaConvertAccess
    with Logging {
  override def handle(upload: Upload): Upload = {
    upload.metadata.runtime match {
      case SelfHostedUploadMetadata(ids) =>
        val jobs = ids.map(getJob)
        val progress = upload.progress

        val complete = jobs.forall(_.status == JobStatus.COMPLETE)
        val error = jobs.exists(_.status == JobStatus.ERROR)

        if (error) {
          throw new IllegalStateException(
            s"Transcode failed: [${jobs.map(getDescription).mkString(",")}]"
          )
        } else if (complete) {
          // update the upload metadata with dimension outputs from job
          val videoDimensions = getVideoDimensions(jobs)
          log.info(s"videoDimensions = $videoDimensions")
          val updatedAsset = upload.metadata.asset.map(
            applyDimensionsToAsset(_, videoDimensions)
          )

          upload.copy(
            metadata = upload.metadata.copy(asset = updatedAsset),
            progress = progress.copy(retries = 0, fullyTranscoded = true)
          )
        } else {
          upload.copy(progress = progress.copy(retries = progress.retries + 1))
        }

      case other =>
        throw new IllegalArgumentException(
          s"Unexpected runtime metadata $other"
        )
    }
  }

  private def getJob(id: String): Job = {
    val request = GetJobRequest.builder().id(id).build()
    val response = mediaConvertClient.getJob(request)
    response.job()
  }

  private def getDescription(job: Job): String = {
    job.errorMessage()
  }

  private def getVideoDimensions(
      jobs: List[Job]
  ): Map[String, ImageAssetDimensions] = {
    // these are the requested transcoder outputs
    val outputs = for {
      job <- jobs
      outputGroup <- job.settings().outputGroups().asScala
      output <- outputGroup.outputs().asScala
    } yield output

    // these are the corresponding extra details available after transcoding (including dimensions)
    val outputDetails = for {
      job <- jobs
      outputGroupDetail <- job.outputGroupDetails().asScala
      outputDetail <- outputGroupDetail.outputDetails().asScala
    } yield outputDetail

    outputs
      .zip(outputDetails)
      .collect {
        case (output, outputDetail) if outputDetail.videoDetails != null =>
          val container = output.containerSettings.container
          val nameModifier = output.nameModifier

          val key = container match {
            case ContainerType.MP4 =>
              container.toString + nameModifier // e.g. MP4_720h
            case _ => container.toString // e.g. M3U8
          }

          log.info(s"container - $container")
          log.info(s"name modifier - $nameModifier")
          log.info(s"key - $key")

          // value is the dimensions from the corresponding outputDetail
          key -> ImageAssetDimensions(
            outputDetail.videoDetails().heightInPx,
            outputDetail.videoDetails().widthInPx
          )
      }
      .toMap
  }

  private def applyDimensionsToAsset(
      asset: VideoAsset,
      videoDimensions: Map[String, ImageAssetDimensions]
  ): VideoAsset =
    asset match {
      case SelfHostedAsset(sources) =>
        val updatedSources = sources.map { source =>
          val dimensions: Option[ImageAssetDimensions] =
            (source.mimeType, source.nameModifier) match {
              case (VideoSource.mimeTypeMp4, Some(nameModifier)) =>
                val key = ContainerType.MP4.toString + nameModifier
                log.info(s"retrieving on key - $key")
                videoDimensions.get(key)
              case (VideoSource.mimeTypeMp4, None) =>
                videoDimensions.get(ContainerType.MP4.toString)
              case (VideoSource.mimeTypeM3u8, _) =>
                videoDimensions.get(ContainerType.M3_U8.toString)
              case _ => None
            }
          source.copy(
            width = dimensions.map(_.width),
            height = dimensions.map(_.height)
          )
        }
        SelfHostedAsset(updatedSources)
      case _ => asset
    }

}
