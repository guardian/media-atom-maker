package com.gu.media.upload

import software.amazon.awssdk.services.mediaconvert.model.{ContainerType, GetJobRequest, Job, JobStatus}
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.Platform.{Url, Youtube}
import com.gu.media.model.VideoInput.{mimeTypeM3u8, mimeTypeMp4}
import com.gu.media.model.{Platform, SelfHostedInput, SelfHostedOutput, VideoInput, VideoOutput, YouTubeInput, YouTubeOutput}
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
          // combine the jobs and inputs to construct the outputs

          val videoInputIdMap = buildVideoInputIdMap(upload.metadata.inputs)
          val videoOutputs = getVideoOutputs(videoInputIdMap, jobs)
          log.info(s"videoOutputs = $videoOutputs")

          upload.copy(
            metadata = upload.metadata.copy(outputs = videoOutputs),
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

  private case class VideoInputIdMapKey(platform: Platform, mimeType: Option[String])
  private type VideoInputIdMap = Map[VideoInputIdMapKey, String]

  // Create two-part key (platform and mimeType), with a value of id
  private def buildVideoInputIdMap(
      inputs: List[VideoInput]
  ): VideoInputIdMap = {
    inputs.map {
      case SelfHostedInput(id, platform, mimeType, _) =>
        VideoInputIdMapKey(platform, Some(mimeType)) -> id
      case YouTubeInput(id, platform) =>
        VideoInputIdMapKey(platform, None) -> id
    }
  }.toMap

  private def getVideoOutputs(
      videoInputIdMap: VideoInputIdMap,
      jobs: List[Job]
  ): List[VideoOutput] = {
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
      .flatMap {
        case (output, outputDetail) if outputDetail.videoDetails != null =>
          val containerType = output.containerSettings.container
          val nameModifier = output.nameModifier

          for {
            mimeType <- containerType match {
              case ContainerType.MP4   => Some(mimeTypeMp4)
              case ContainerType.M3_U8 => Some(mimeTypeM3u8)
              case _                   => None
            }
          } yield {
            // Might be possible to extract at the very end? https://docs.aws.amazon.com/mediaconvert/latest/ug/output-file-names-and-paths.html
            val idWithModifier = containerType match {
              case ContainerType.MP4 =>
                val id = videoInputIdMap(VideoInputIdMapKey(Url, Some(mimeTypeMp4)))
                id.dropRight(4) + nameModifier + ".mp4"
              case ContainerType.M3_U8 =>
                val id = videoInputIdMap(VideoInputIdMapKey(Url, Some(mimeTypeM3u8)))
                id.dropRight(5) + ".m3u8"
              // TODO: not sure on this
              case _ => videoInputIdMap(VideoInputIdMapKey(Youtube, None))
            }
            val height = outputDetail.videoDetails().heightInPx
            val width = outputDetail.videoDetails().widthInPx
            SelfHostedOutput(
              id = idWithModifier,
              mimeType,
              height = Some(height),
              width = Some(width)
            )
          }
        case _ => None
      }
  }
}
