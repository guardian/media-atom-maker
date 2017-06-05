package com.gu.media.upload

import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class AddSelfHostedAsset extends LambdaWithParams[Upload, Upload] with Logging {
  override def handle(upload: Upload): Upload = {
    // TODO MRB: add self hosted asset
    log.info("Add self hosted asset to atom (not really")

    upload
  }
}
