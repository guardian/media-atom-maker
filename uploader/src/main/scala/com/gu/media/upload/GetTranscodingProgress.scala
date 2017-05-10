package com.gu.media.upload

import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class GetTranscodingProgress extends LambdaWithParams[Upload, Upload] with Logging {
  override def handle(upload: Upload): Upload = {
    log.info(s"Checking transcoding of ${upload.metadata.pluto.s3Key} is complete (not really!!)")

    // Simulate transcoding using retries!
    val progress = upload.progress

    if(progress.retries == 3) {
      upload.copy(progress = progress.copy(retries = 0, fullyTranscoded = true))
    } else {
      upload.copy(progress = progress.copy(retries = progress.retries + 1))
    }
  }
}
