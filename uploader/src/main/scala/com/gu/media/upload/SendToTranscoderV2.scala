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
}
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedInput, VideoInput}
import com.gu.media.upload.model.{SelfHostedUploadMetadata, Upload}

import scala.jdk.CollectionConverters._

class SendToTranscoderV2
    extends LambdaWithParams[Upload, Upload]
    with MediaConvertAccess
    with Logging {
  override def handle(upload: Upload): Upload = {
    val videoInput = Upload.videoInputUri(upload)
    val maybeSubtitlesInput = Upload.subtitleInputUri(upload)

    val selfHostedInputs = upload.metadata.inputs.collect { case input: SelfHostedInput => input }
    val outputGroups = getOutputGroups(selfHostedInputs)
    val jobs = sendToTranscoder(videoInput, maybeSubtitlesInput, outputGroups)

    val metadata =
      upload.metadata.copy(runtime = SelfHostedUploadMetadata(List(jobs)))
    upload.copy(
      metadata = metadata,
      progress = upload.progress.copy(fullyTranscoded = false)
    )
  }

  private def sendToTranscoder(
                                videoInput: UploadUri,
                                maybeSubtitlesInput: Option[UploadUri],
                                outputGroups: List[OutputGroup]
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
      .settings(
        JobSettings
          .builder()
          .inputs(List(jobInput).asJava)
          .outputGroups(outputGroups: _*)
          .build()
      )
      .build()

    log.info("Creating job: " + createJobRequest.toString)

    val job = mediaConvertClient.createJob(createJobRequest).job()
    val id = job.id()

    log.info(s"Sent $videoInput to mediaconvert (id: $id)")

    id
  }

  private def getOutputGroups(selfHostedInputs: List[SelfHostedInput]): List[OutputGroup] = {
    selfHostedInputs
      .map {
        case SelfHostedInput(output, VideoInput.mimeTypeMp4, _) =>
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

        case SelfHostedInput(output, VideoInput.mimeTypeM3u8, _) =>
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

        case SelfHostedInput(_, other, _) =>
          throw new IllegalArgumentException(s"Unsupported mime type $other")
      }
  }
}
