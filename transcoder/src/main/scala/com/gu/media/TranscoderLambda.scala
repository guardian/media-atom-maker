package com.gu.media

import java.io.{InputStream, OutputStream}

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.amazonaws.services.elastictranscoder.model.{CreateJobOutput, CreateJobRequest, JobInput}
import com.amazonaws.services.lambda.runtime.{Context, LambdaLogger, RequestStreamHandler}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.{Json, _}


case class JobInfo(pipelineId: String, masterFileName: String)

object JobInfo {
  implicit val format: Format[JobInfo] = Jsonx.formatCaseClass[JobInfo]
}

class TranscoderLambda extends RequestStreamHandler {

  val PRESET_ID = "1351620000001-000001" //System preset: Generic 1080p

  def handleRequest(input: InputStream, output: OutputStream, context: Context) = {

    val logger: LambdaLogger = context.getLogger

    logger.log("Starting job")
    val jobInfo = Json.parse(input).as[JobInfo]
    if (jobInfo.masterFileName.endsWith("mxf")) {
      val transcoderClient = new AmazonElasticTranscoderClient()
      transcoderClient.setRegion(Region.getRegion(Regions.EU_WEST_1))

      val jobInput = new JobInput().withKey(jobInfo.masterFileName)

      val mp4FileName = jobInfo.masterFileName.replace(".mxf", ".mp4")

      val jobOutput = new CreateJobOutput()
        .withKey(s"$mp4FileName")
        .withPresetId(PRESET_ID)

      // Create the job.
      val createJobRequest = new CreateJobRequest()
        .withPipelineId(jobInfo.pipelineId)
        .withInput(jobInput)
        .withOutput(jobOutput)

      transcoderClient.createJob(createJobRequest).getJob
      logger.log("Job started")


    } else {
      logger.log(s"Expected a filename ending in mxf instead received ${jobInfo.masterFileName}")
    }

  }
}
