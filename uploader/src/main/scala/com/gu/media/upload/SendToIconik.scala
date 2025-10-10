package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, KinesisAccess, SESSettings}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class SendToIconik extends LambdaWithParams[Upload, Upload]
  with DynamoAccess
  with KinesisAccess
  with SESSettings
  with Logging
{
  override def handle(upload: Upload): Upload = {
    log.info(s"Mock sending to Iconik. Asset(s): ${upload.metadata.asset}. AtomId: ${upload.metadata.pluto.atomId}")
    upload
  }
}
