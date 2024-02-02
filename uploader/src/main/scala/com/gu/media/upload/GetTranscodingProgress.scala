package com.gu.media.upload

import com.amazonaws.services.elastictranscoder.model.{Job, ReadJobRequest}
import com.gu.media.aws.ElasticTranscodeAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.{SelfHostedUploadMetadata, Upload}

import scala.jdk.CollectionConverters._

class GetTranscodingProgress extends LambdaWithParams[Upload, Upload] with ElasticTranscodeAccess with Logging {
  override def handle(upload: Upload): Upload = {
    upload.metadata.runtime match {
      case SelfHostedUploadMetadata(ids) =>
        val jobs = ids.map(getJob)
        val progress = upload.progress

        val complete = jobs.forall(_.getStatus == "Complete")
        val error = jobs.exists(_.getStatus == "Error")

        if(error) {
          throw new IllegalStateException(s"Transcode failed: [${jobs.map(getDescription).mkString(",")}]")
        } else if(complete) {
          upload.copy(progress = progress.copy(retries = 0, fullyTranscoded = true))
        } else {
          upload.copy(progress = progress.copy(retries = progress.retries + 1))
        }

      case other =>
        throw new IllegalArgumentException(s"Unexpected runtime metadata $other")
    }
  }

  private def getJob(id: String): Job = {
    val request = new ReadJobRequest().withId(id)
    val response = transcoderClient.readJob(request)

    response.getJob
  }

  private def getDescription(job: Job): String = {
    job.getOutputs.asScala
      .map { job => Some(job.getStatusDetail).getOrElse("unknown") }.mkString(", ")
  }
}
