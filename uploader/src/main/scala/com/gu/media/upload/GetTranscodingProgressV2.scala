package com.gu.media.upload

import com.amazonaws.services.mediaconvert.model.{Job, GetJobRequest}
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.{SelfHostedUploadMetadata, Upload}

class GetTranscodingProgressV2
    extends LambdaWithParams[Upload, Upload]
    with MediaConvertAccess
    with Logging {
  override def handle(upload: Upload): Upload = {
    upload.metadata.runtime match {
      case SelfHostedUploadMetadata(ids) =>
        val jobs = ids.map(getJob)
        val progress = upload.progress

        val complete = jobs.forall(_.getStatus == "COMPLETE")
        val error = jobs.exists(_.getStatus == "ERROR")

        if (error) {
          throw new IllegalStateException(
            s"Transcode failed: [${jobs.map(getDescription).mkString(",")}]"
          )
        } else if (complete) {
          upload.copy(progress =
            progress.copy(retries = 0, fullyTranscoded = true)
          )
        } else {
          upload.copy(progress = progress.copy(retries = progress.retries + 1))
        }

      case other =>
        throw new IllegalArgumentException(
          s"Unexpected runtime metadata $other"
        )
    }
  }

  private def getJob(id: String): Job = {
    val request = new GetJobRequest().withId(id)
    val response = mediaConvertClient.getJob(request)

    response.getJob
  }

  private def getDescription(job: Job): String = {
    job.getErrorMessage
  }
}
