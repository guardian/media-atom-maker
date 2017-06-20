package com.gu.media.upload

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.elastictranscoder.model.{CreateJobOutput, CreateJobRequest, JobInput}
import com.gu.media.aws.ElasticTranscodeAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoSource}
import com.gu.media.upload.model.Upload

class SendToTranscoder extends LambdaWithParams[Upload, Upload]
  with ElasticTranscodeAccess
  with Logging
{
  transcoderClient.setRegion(Region.getRegion(Regions.EU_WEST_1))

  private val preset720p = "1351620000001-000010"

  override def handle(upload: Upload): Upload = {
    val input = upload.metadata.pluto.s3Key
    val output = getOutput(upload)

    val id = sendToTranscoder(input, output)
    upload.copy(metadata = upload.metadata.copy(mp4TranscodeJobId = Some(id)))
  }

  private def sendToTranscoder(input: String, output: String): String = {
    val jobInput = new JobInput().withKey(input)

    val jobOutput = new CreateJobOutput()
      .withKey(output)
      .withPresetId(preset720p)

    val createJobRequest = new CreateJobRequest()
      .withPipelineId(transcodePipelineId)
      .withInput(jobInput)
      .withOutput(jobOutput)

    val job = transcoderClient.createJob(createJobRequest).getJob
    val id = job.getId

    log.info(s"Sent $input to transcoder (id: $id) output will be $output")

    id
  }

  private def getOutput(upload: Upload): String = {
    upload.metadata.asset.collect {
      case SelfHostedAsset(VideoSource(output, "video/mp4") :: Nil) => output
    }.getOrElse {
      throw new IllegalStateException("Missing MP4 asset")
    }
  }
}
