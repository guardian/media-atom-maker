package com.gu.media.upload

import com.amazonaws.services.elastictranscoder.model.{Job, ReadJobRequest}
import com.gu.media.aws.ElasticTranscodeAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload
import scala.collection.JavaConverters._

class GetTranscodingProgress extends LambdaWithParams[Upload, Upload] with ElasticTranscodeAccess with Logging {
  override def handle(upload: Upload): Upload = {
    val progress = upload.progress

    val id = upload.metadata.mp4TranscodeJobId.getOrElse { throw new IllegalStateException("Missing MP4 job ID") }
    val job = getJob(id)

    log.info(s"Job ${job.getId}: ${job.getStatus} $job")

    job.getStatus match {
      case "Complete" =>
        upload.copy(progress = progress.copy(retries = 0, fullyTranscoded = true))

      case "Error" =>
        throw new IllegalStateException(s"Transcode failed: ${getDescription(job)}")

      case _ =>
        upload.copy(progress = progress.copy(retries = progress.retries + 1))
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
