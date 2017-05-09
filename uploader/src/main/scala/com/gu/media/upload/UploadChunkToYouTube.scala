package com.gu.media.upload

import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class UploadChunkToYouTube extends LambdaWithParams[Upload, Upload] with Logging {
  override def handle(upload: Upload): Upload = {
    val chunk = upload.parts(upload.progress.chunksInS3 - 1)
    log.info(s"Uploading ${chunk.key} to YouTube (not really!!)")

    upload
  }
}
