package com.gu.media

import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

// TODO MRB: remove this when we move to step functions
trait UploadLambda { this: LambdaWithParams[Upload, Upload] with Logging =>
  def handleUpload(upload: Upload): Upload

  final override def handle(upload: Upload): Upload = {
    if(upload.metadata.useStepFunctions) {
      handleUpload(upload)
    } else {
      log.info("Bypassing step function")
      upload
    }
  }
}
