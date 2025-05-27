package com.gu.media.upload

import com.amazonaws.services.mediaconvert.model.{CaptionSelector, CaptionSourceSettings, CreateJobRequest, FileGroupSettings, FileSourceSettings, HlsGroupSettings, Input, JobSettings, OutputGroup, OutputGroupSettings}
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoSource}
import com.gu.media.upload.model.{SelfHostedUploadMetadata, Upload}

import scala.jdk.CollectionConverters._

class SendToTranscoderV2 extends LambdaWithParams[Upload, Upload]
  with MediaConvertAccess
  with Logging
{
  override def handle(upload: Upload): Upload = {
    val input = s"s3://${upload.metadata.bucket}/${upload.metadata.pluto.s3Key}"

    upload.metadata.asset match {
      case Some(SelfHostedAsset(sources)) =>
        val outputs = getOutputs(sources)
        val jobs = sendToTranscoder(input, outputs)

        val metadata = upload.metadata.copy(runtime = SelfHostedUploadMetadata(List(jobs)))
        upload.copy(metadata = metadata, progress = upload.progress.copy(fullyTranscoded = false))

      case other =>
        throw new IllegalArgumentException(s"Unexpected asset $other")
    }
  }

  private def sendToTranscoder(input: String, outputs: List[OutputGroup]): String = {
    val jobInput = new Input()
      .withFileInput(input)

    val jobTemplate = s"media-atom-maker-transcoder-${stage}"

    val createJobRequest = new CreateJobRequest()
      .withRole(mediaConvertRole)
      .withJobTemplate(jobTemplate)
      .withSettings(new JobSettings()
        .withInputs(List(jobInput).asJava)
        .withOutputGroups(outputs: _*)
      )

    val job = mediaConvertClient.createJob(createJobRequest).getJob
    val id = job.getId

    log.info(s"Sent $input to mediaconvert (id: $id)")

    id
  }

  private def getOutputs(sources: List[VideoSource]): List[OutputGroup] = {
    sources.map {
      case VideoSource(output, "video/mp4") =>
        val outputGroupSettings = new OutputGroupSettings()
          .withFileGroupSettings(new FileGroupSettings()
            .withDestination(output)
          )
        new OutputGroup().withOutputGroupSettings(outputGroupSettings)

      case VideoSource(output, "application/vnd.apple.mpegurl") =>
        val outputGroupSettings = new OutputGroupSettings()
          .withHlsGroupSettings(new HlsGroupSettings()
            .withDestination(output)
          )
        new OutputGroup().withOutputGroupSettings(outputGroupSettings)

      case VideoSource(_, other) =>
        throw new IllegalArgumentException(s"Unsupported mime type $other")
    }
  }
}
