package com.gu.media.upload

import com.amazonaws.services.mediaconvert.model.{CaptionSelector, CaptionSourceSettings, CreateJobRequest, FileSourceSettings, HlsGroupSettings, Input, JobSettings, OutputGroup, OutputGroupSettings}
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoSource}
import com.gu.media.upload.model.{SelfHostedUploadMetadata, Upload}

import scala.jdk.CollectionConverters._

// need to test that any of this works
// first step - set up a new template loosely based on my working transcode
// then try testing from console similar to GetTranscodingProgressV2
class SendToTranscoderV2 extends LambdaWithParams[Upload, Upload]
  with MediaConvertAccess
  with Logging
{
  override def handle(upload: Upload): Upload = {
    val input = s"s3://${upload.metadata.bucket}/${upload.metadata.pluto.s3Key}"

    upload.metadata.asset match {
      case Some(SelfHostedAsset(sources)) =>
        val outputs = getOutputs(sources)
        val jobs = outputs.map { case output => sendToTranscoder(input, upload.metadata.subtitlesS3Key, output) }

        val metadata = upload.metadata.copy(runtime = SelfHostedUploadMetadata(jobs))
        upload.copy(metadata = metadata, progress = upload.progress.copy(fullyTranscoded = false))

      case other =>
        throw new IllegalArgumentException(s"Unexpected asset $other")
    }
  }

  private def sendToTranscoder(input: String, subtitles: Option[String], output: String): String = {
    val baseJobInput = new Input()
      .withFileInput(input)

    val jobInput = subtitles.map(subs => baseJobInput
      .withCaptionSelectors(Map(
        "Captions Selector 1" -> new CaptionSelector()
          .withSourceSettings(new CaptionSourceSettings()
            .withSourceType("SRT")
            .withFileSourceSettings(new FileSourceSettings()
              .withSourceFile(subs)
            )
          )
      ).asJava)
    ).getOrElse(baseJobInput)

    val outputGroupSettings = new OutputGroupSettings()
      .withHlsGroupSettings(new HlsGroupSettings()
        .withDestination(output)
      )
    val outputGroup = new OutputGroup().withOutputGroupSettings(outputGroupSettings)

    val jobTemplate = s"media-atom-maker-transcoder-${stage}${if (subtitles.isDefined) "-with-subtitles"}"

    val createJobRequest = new CreateJobRequest()
      .withRole("arn:aws:iam::563563610310:role/service-role/MediaConvert_DF_Test_Role") // todo
      .withJobTemplate(jobTemplate)
      .withSettings(new JobSettings()
        .withInputs(List(jobInput).asJava)
        .withOutputGroups(outputGroup)
      )

    val job = mediaConvertClient.createJob(createJobRequest).getJob
    val id = job.getId

    log.info(s"Sent $input to transcoder (id: $id) output will be $output")

    id
  }

  private def getOutputs(sources: List[VideoSource]): List[String] = {
    sources.map {
      case VideoSource(output, "video/mp4") =>
        output

      case VideoSource(_, other) =>
        throw new IllegalArgumentException(s"Unsupported mime type $other")
    }
  }
}
