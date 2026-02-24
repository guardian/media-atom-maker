package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, KinesisAccess, SESSettings, SQSAccess}
import com.gu.media.iconik.{AssetUploadedToAtomMessage, IconikUploadActions}
import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging
import com.gu.media.ses.Mailer
import com.gu.media.upload.model.Upload

class SendToPluto
    extends LambdaWithParams[Upload, Upload]
    with LambdaBase
    with DynamoAccess
    with KinesisAccess
    with SESSettings
    with SQSAccess
    with Logging {
  private val pluto = new PlutoUploadActions(this)
  private val iconik = new IconikUploadActions(this)
  private val mailer = new Mailer(this)

  override def handle(upload: Upload): Upload = {
    pluto.sendToPluto(upload.metadata.pluto)
    iconik.sendMessage(
      AssetUploadedToAtomMessage(
        upload.metadata.pluto.atomId,
        upload.metadata.pluto.s3Key,
        upload.metadata.iconikData
      )
    )

    val plutoData = upload.metadata.pluto

    val shouldSendEmailReminder =
      plutoData.user != this.integrationTestUser && this.syncWithPluto

    if (shouldSendEmailReminder) {
      log.info(
        s"Sending missing Pluto ID email user=${plutoData.user} atom=${plutoData.atomId}"
      )

      mailer.sendPlutoIdMissingEmail(
        plutoData.atomId,
        plutoData.title,
        plutoData.user
      )
    }

    upload
  }
}
