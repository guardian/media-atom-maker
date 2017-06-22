package com.gu.media.upload

import com.amazonaws.services.elastictranscoder.model.{CreateJobOutput, CreateJobRequest, JobInput}
import com.gu.media.aws.ElasticTranscodeAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoSource}
import com.gu.media.upload.model.{SelfHostedUploadMetadata, Upload}

class SendToTranscoder extends LambdaWithParams[Upload, Upload]
  with ElasticTranscodeAccess
  with Logging
{
  override def handle(upload: Upload): Upload = {
    val input = upload.metadata.pluto.s3Key

    upload.metadata.asset match {
      case Some(SelfHostedAsset(sources)) =>
        val outputs = getOutputs(sources)
        val jobs = outputs.map { case(output, preset) => sendToTranscoder(input, output, preset) }

        val metadata = upload.metadata.copy(runtime = Some(SelfHostedUploadMetadata(jobs)))
        upload.copy(metadata = metadata)

      case other =>
        throw new IllegalArgumentException(s"Unexpected asset $other")
    }
  }

  private def sendToTranscoder(input: String, output: String, preset: String): String = {
    val jobInput = new JobInput().withKey(input)

    val jobOutput = new CreateJobOutput()
      .withKey(output)
      .withPresetId(preset)

    val createJobRequest = new CreateJobRequest()
      .withPipelineId(transcodePipelineId)
      .withInput(jobInput)
      .withOutput(jobOutput)

    val job = transcoderClient.createJob(createJobRequest).getJob
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
