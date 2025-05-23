package com.gu.media.upload

//import com.amazonaws.services.elastictranscoder.model.{CreateJobOutput, CreateJobRequest, JobInput}

import com.amazonaws.services.mediaconvert.model.{CaptionSelector, CaptionSourceSettings, CreateJobRequest, FileSourceSettings, HlsGroupSettings, Input, JobSettings, OutputGroup, OutputGroupSettings}
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
    val input = upload.metadata.pluto.s3Key

    upload.metadata.asset match {
      case Some(SelfHostedAsset(sources)) =>
        val outputs = getOutputs(sources)
        val jobs = outputs.map { case(output, preset) => sendToTranscoder(input, output, preset) }

        val metadata = upload.metadata.copy(runtime = SelfHostedUploadMetadata(jobs))
        upload.copy(metadata = metadata)

      case other =>
        throw new IllegalArgumentException(s"Unexpected asset $other")
    }
  }

  private def sendToTranscoder(input: String, output: String, preset: String): String = {
    val jobInput = new Input()
      .withFileInput(input)
      .withCaptionSelectors(Map(
        "Captions Selector 1" -> new CaptionSelector()
          .withSourceSettings(new CaptionSourceSettings()
            .withSourceType("SRT")
            .withFileSourceSettings(new FileSourceSettings()
            .withSourceFile(s"$input.srt"))
          )
      ).asJava)

    val outputGroupSettings = new OutputGroupSettings()
      .withHlsGroupSettings(new HlsGroupSettings()
        .withDestination(output)
      )
    val outputGroup = new OutputGroup().withOutputGroupSettings(outputGroupSettings)

    val createJobRequest = new CreateJobRequest()
      .withRole("todo")
      .withJobTemplate("media-atom-maker-transcoder-CODE")
      .withSettings(new JobSettings()
        .withInputs(List(jobInput).asJava)
        .withOutputGroups(outputGroup)
      )

    val job = mediaConvertClient.createJob(createJobRequest).getJob
    val id = job.getId

    log.info(s"Sent $input to transcoder (id: $id, preset $preset) output will be $output")

    id
  }

  private def getOutputs(sources: List[VideoSource]): List[(String, String)] = {
    sources.map {
      case VideoSource(output, "video/mp4") =>
        (output, "1351620000001-000010") // generic 720p

      case VideoSource(_, other) =>
        throw new IllegalArgumentException(s"Unsupported mime type $other")
    }
  }
}
