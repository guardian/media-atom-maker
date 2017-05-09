package com.gu.media.upload

import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class SendToTranscoder extends LambdaWithParams[Upload, Upload] with Logging {
  override def handle(upload: Upload): Upload = {
    log.info(s"Sending ${upload.metadata.pluto.s3Key} to be transcoded (not really!!)")

    upload
  }
}
