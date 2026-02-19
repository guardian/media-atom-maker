package com.gu.media.upload

import software.amazon.awssdk.services.mediaconvert.model.{ContainerType, GetJobRequest, Job, JobStatus}
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.VideoInput.{mimeTypeM3u8, mimeTypeMp4}
import com.gu.media.model.{SelfHostedOutput, VideoOutput}
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
          // update the upload metadata with outputs from job
          val videoOutputs = getVideoOutputs(jobs)
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

  private def getVideoOutputs(
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
      .collect {
        case (output, outputDetail) if outputDetail.videoDetails != null =>
          val container = output.containerSettings.container
          val nameModifier = output.nameModifier
          val extension = output.extension

          val id = container match {
            case ContainerType.MP4 => container.toString + nameModifier + extension // e.g. MP4_720h.mp4
            case _ => container.toString + extension // e.g. M3U8.m3u8
          }

          val mimeType = container match {
            case ContainerType.MP4 => mimeTypeMp4
            case ContainerType.M3_U8 => mimeTypeM3u8
          }

          val height = outputDetail.videoDetails().heightInPx
          val width = outputDetail.videoDetails().widthInPx

          SelfHostedOutput(
            id, mimeType, height = Some(height), width = Some(width)
          )
      }
  }
}
