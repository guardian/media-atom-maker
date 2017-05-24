package com.gu.media.upload

import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class CreateCompleteVideoInS3 extends LambdaWithParams[Upload, Upload] with Logging {
  override def handle(upload: Upload): Upload = {
    log.info(s"Creating complete video ${upload.metadata.pluto.s3Key} in S3 (not really!!)")

    upload
  }
}
