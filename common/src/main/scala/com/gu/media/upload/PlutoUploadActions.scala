package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, KinesisAccess, SESSettings}
import com.gu.media.logging.Logging
import com.gu.media.ses.Mailer
import com.gu.media.upload.model.UploadMetadata
import com.gu.media.{PlutoDataStore, Settings}

class PlutoUploadActions(config: Settings with DynamoAccess with KinesisAccess with SESSettings) extends Logging {
  private val mailer = new Mailer(config.sesClient, config.getMandatoryString("host"))
  private val plutoStore = new PlutoDataStore(config.dynamoDB, config.manualPlutoDynamo)

  def sendToPluto(metadata: UploadMetadata): Unit = {
    val plutoData = metadata.pluto

    plutoData.projectId match {
      case Some(project) =>
        config.sendOnKinesis(config.uploadsStreamName, plutoData.s3Key, plutoData)

      case None =>
        log.info(s"Sending missing Pluto ID email user=${metadata.user} atom=${plutoData.atomId}")

        mailer.sendPlutoIdMissingEmail(
          metadata.title,
          metadata.user,
          config.fromEmailAddress,
          config.replyToAddresses)

        plutoStore.put(plutoData)
    }
  }
}
