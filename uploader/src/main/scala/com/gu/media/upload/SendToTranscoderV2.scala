package com.gu.media.upload

import software.amazon.awssdk.services.mediaconvert.model.{
  CaptionSelector,
  CaptionSourceSettings,
  CreateJobRequest,
  FileGroupSettings,
  FileSourceSettings,
  HlsGroupSettings,
  Input,
  JobSettings,
  OutputGroup,
  OutputGroupSettings,
  OutputGroupType
}
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoSource}
import com.gu.media.upload.model.{
  SelfHostedUploadMetadata,
  Upload,
  WaitOnUpload
}

import scala.jdk.CollectionConverters._

class SendToTranscoderV2
    extends LambdaWithParams[WaitOnUpload, Upload]
    with LambdaBase
    with MediaConvertAccess
    with Logging {
  override def handle(data: WaitOnUpload): Upload = {
    val upload = data.input
    val videoInput = Upload.videoInputUri(upload)
    val maybeSubtitlesInput = Upload.subtitleInputUri(upload)

    upload.metadata.asset match {
      case Some(SelfHostedAsset(sources)) =>
        val outputs = getOutputs(sources)
        val jobs = sendToTranscoder(
          videoInput,
          maybeSubtitlesInput,
          outputs,
          data.taskToken,
          data.executionId
        )

        val metadata =
          upload.metadata.copy(runtime = SelfHostedUploadMetadata(List(jobs)))
        upload.copy(
          metadata = metadata,
          progress = upload.progress.copy(fullyTranscoded = false)
        )

      case other =>
        throw new IllegalArgumentException(s"Unexpected asset $other")
    }
  }

  private def sendToTranscoder(
      videoInput: UploadUri,
      maybeSubtitlesInput: Option[UploadUri],
      outputs: List[OutputGroup],
      taskToken: String,
      executionId: String
  ): String = {
    val captionSourceSettings = maybeSubtitlesInput match {
      case Some(subtitlesInput) =>
        CaptionSourceSettings
          .builder()
          .sourceType("SRT")
          .fileSourceSettings(
            FileSourceSettings
              .builder()
              .sourceFile(subtitlesInput.toString)
              .build()
          )
          .build()
      case None =>
        CaptionSourceSettings.builder().sourceType("NULL_SOURCE").build()
    }

    val captionSelectors = Map(
      "Caption Selector 1" -> CaptionSelector
        .builder()
        .sourceSettings(
          captionSourceSettings
        )
        .build()
    ).asJava

    val jobInput = Input
      .builder()
      .fileInput(videoInput.toString)
      .captionSelectors(captionSelectors)
      .build()

    val jobTemplate = s"media-atom-maker-transcoder-${stage}"

    val createJobRequest = CreateJobRequest
      .builder()
      .role(mediaConvertRole)
      .jobTemplate(jobTemplate)
      .userMetadata(
        Map(
          "stage" -> stage,
          "executionId" -> executionId
        ).asJava
      )
      .settings(
        JobSettings
          .builder()
          .inputs(List(jobInput).asJava)
          .outputGroups(outputs: _*)
          .build()
      )
      .build()

    log.info("Creating job: " + createJobRequest.toString)

    val job = mediaConvertClient.createJob(createJobRequest).job()
    val id = job.id()

    log.info(s"Sent $videoInput to mediaconvert (id: $id)")

    id
  }

  private def getOutputs(sources: List[VideoSource]): List[OutputGroup] = {
    sources.map {
      case VideoSource(output, VideoSource.mimeTypeMp4, _, _) =>
        val filenameWithoutMp4 =
          if (output.endsWith(".mp4")) output.dropRight(4) else output
        val outputGroupSettings = OutputGroupSettings
          .builder()
          .fileGroupSettings(
            FileGroupSettings
              .builder()
              .destination(
                UploadUri(destinationBucket, filenameWithoutMp4).toString
              )
              .build()
          )
          .build()
        OutputGroup.builder().outputGroupSettings(outputGroupSettings).build()

      case VideoSource(output, VideoSource.mimeTypeM3u8, _, _) =>
        val filenameWithoutM3u8 =
          if (output.endsWith(".m3u8")) output.dropRight(5) else output
        val outputGroupSettings = OutputGroupSettings
          .builder()
          .hlsGroupSettings(
            HlsGroupSettings
              .builder()
              .destination(
                UploadUri(destinationBucket, filenameWithoutM3u8).toString
              )
              .segmentLength(10)
              .minSegmentLength(0)
              .build()
          )
          .build()
        OutputGroup.builder().outputGroupSettings(outputGroupSettings).build()

      case VideoSource(_, other, _, _) =>
        throw new IllegalArgumentException(s"Unsupported mime type $other")
    }
  }
}
