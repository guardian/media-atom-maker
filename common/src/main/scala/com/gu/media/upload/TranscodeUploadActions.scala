package com.gu.media.upload

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.amazonaws.services.elastictranscoder.model.{CreateJobOutput, CreateJobRequest, JobInput}
import com.gu.media.logging.Logging

class TranscodeUploadActions(transcoderClient: AmazonElasticTranscoderClient) extends Logging {
  transcoderClient.setRegion(Region.getRegion(Regions.EU_WEST_1))
  private val TRANSCODER_PRESET_ID = "1351620000001-000001" //System preset: Generic 1080p

  def transcodeToS3(fileName: String, pipelineId: String) {

    val jobInput = new JobInput().withKey(fileName)

    val mp4FileName = fileName + ".mp4"

    val jobOutput = new CreateJobOutput()
      .withKey(mp4FileName)
      .withPresetId(TRANSCODER_PRESET_ID)

    val createJobRequest = new CreateJobRequest()
      .withPipelineId(pipelineId)
      .withInput(jobInput)
      .withOutput(jobOutput)

    transcoderClient.createJob(createJobRequest).getJob
    log.info(s"Sent $fileName to transcoder output will be $mp4FileName")
  }
}
