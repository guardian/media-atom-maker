package com.gu.media.upload

import com.gu.media.UploadLambda
import com.gu.media.aws.{DynamoAccess, KinesisAccess, SESSettings}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class SendToPluto extends LambdaWithParams[Upload, Upload]
  with DynamoAccess
  with KinesisAccess
  with SESSettings
  with Logging
  with UploadLambda
{
  private val pluto = new PlutoUploadActions(this)

  override def handleUpload(upload: Upload): Upload = {
    pluto.sendToPluto(upload.metadata)
    upload
  }
}
