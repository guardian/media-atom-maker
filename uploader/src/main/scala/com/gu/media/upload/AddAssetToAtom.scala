package com.gu.media.upload

import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class AddAssetToAtom extends LambdaWithParams[Upload, Upload] with Logging {
  override def handle(upload: Upload): Upload = {
    log.info(s"Adding asset to ${upload.metadata.pluto.atomId} (not really!!)")

    upload
  }
}
