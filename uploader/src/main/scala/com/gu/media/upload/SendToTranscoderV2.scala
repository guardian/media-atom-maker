package com.gu.media.upload

import com.amazonaws.services.mediaconvert.model.{CaptionSelector, CaptionSourceSettings, CreateJobRequest, FileGroupSettings, FileSourceSettings, HlsGroupSettings, Input, JobSettings, OutputGroup, OutputGroupSettings, OutputGroupType}
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
    val videoInput = s"s3://${upload.metadata.bucket}/${upload.metadata.pluto.s3Key}"
    val maybeSubtitlesInput = upload.metadata.subtitleSource.map(subtitleSource => s"s3://${upload.metadata.bucket}/${subtitleSource.src}")

    upload.metadata.asset match {
      case Some(SelfHostedAsset(sources)) =>
        val outputs = getOutputs(sources)
        val jobs = sendToTranscoder(videoInput, maybeSubtitlesInput, outputs)

        val metadata = upload.metadata.copy(runtime = SelfHostedUploadMetadata(List(jobs)))
        upload.copy(metadata = metadata, progress = upload.progress.copy(fullyTranscoded = false))

      case other =>
        throw new IllegalArgumentException(s"Unexpected asset $other")
    }
  }

  private def sendToTranscoder(videoInput: String, maybeSubtitlesInput: Option[String], outputs: List[OutputGroup]): String = {
    val captionSourceSettings = maybeSubtitlesInput match {
      case Some(subtitlesInput) =>
        new CaptionSourceSettings()
          .withSourceType("SRT")
          .withFileSourceSettings(new FileSourceSettings()
            .withSourceFile(subtitlesInput)
          )
      case None => new CaptionSourceSettings().withSourceType("NULL_SOURCE")
    }

    val captionSelectors = Map(
      "Caption Selector 1" -> new CaptionSelector().withSourceSettings(captionSourceSettings)
    ).asJava

    val jobInput = new Input()
      .withFileInput(videoInput)
      .withCaptionSelectors(captionSelectors)

    val jobTemplate = s"media-atom-maker-transcoder-${stage}"

    val createJobRequest = new CreateJobRequest()
      .withRole(mediaConvertRole)
      .withJobTemplate(jobTemplate)
      .withSettings(new JobSettings()
        .withInputs(List(jobInput).asJava)
        .withOutputGroups(outputs: _*)
      )

    log.info("Creating job: "+createJobRequest.toString)

    val job = mediaConvertClient.createJob(createJobRequest).getJob
    val id = job.getId

    log.info(s"Sent $videoInput to mediaconvert (id: $id)")

    id
  }

  private def getOutputs(sources: List[VideoSource]): List[OutputGroup] = {
    sources.map {
      case VideoSource(output, "video/mp4") =>
        val filenameWithoutMp4 = if (output.endsWith(".mp4")) output.dropRight(4) else output
        val outputGroupSettings = new OutputGroupSettings()
          .withFileGroupSettings(new FileGroupSettings()
            .withDestination(s"s3://${destinationBucket}/$filenameWithoutMp4")
          )
        new OutputGroup().withOutputGroupSettings(outputGroupSettings)

      case VideoSource(output, "application/vnd.apple.mpegurl") =>
        val outputGroupSettings = new OutputGroupSettings()
          .withHlsGroupSettings(new HlsGroupSettings()
            .withDestination(s"s3://${destinationBucket}/$output")
            .withSegmentLength(10)
            .withMinSegmentLength(0)
          )
        new OutputGroup().withOutputGroupSettings(outputGroupSettings)

      case VideoSource(_, other) =>
        throw new IllegalArgumentException(s"Unsupported mime type $other")
    }
  }
}
