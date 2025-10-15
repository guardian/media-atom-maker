package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, KinesisAccess, SESSettings}
import com.gu.media.logging.Logging
import com.gu.media.model._
import com.gu.media.ses.Mailer
import com.gu.media.{PlutoDataStore, Settings}

class PlutoUploadActions(
    config: Settings with DynamoAccess with KinesisAccess with SESSettings
) extends Logging {
  private val mailer = new Mailer(config)
  private val plutoStore =
    new PlutoDataStore(config.scanamo, config.manualPlutoDynamo)

  def sendToPluto(plutoIntegrationMessage: PlutoIntegrationMessage): Unit = {

    sendKinesisMessage(plutoIntegrationMessage)

    plutoIntegrationMessage match {
      case plutoData: PlutoSyncMetadataMessage
          if plutoData.projectId.isEmpty => {
        plutoStore.put(plutoData)

        val shouldSendEmailReminder =
          plutoData.user != config.integrationTestUser && config.syncWithPluto

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
      }
      case _ =>
      // there is nothing extra to do for AtomAssignedProjectMessage, PacFileMessage, or PlutoResyncMetadataMessage
    }
  }

  private def sendKinesisMessage(
      plutoIntegrationMessage: PlutoIntegrationMessage
  ): Unit = {
    if (config.syncWithPluto) {
      log.info(
        s"writing message to pluto integration stream: type=${plutoIntegrationMessage.`type`} atomId=${plutoIntegrationMessage.atomId} content=$plutoIntegrationMessage"
      )
      config.sendOnKinesis(
        config.plutoIntegrationOutgoingStream,
        plutoIntegrationMessage.partitionKey,
        plutoIntegrationMessage
      )
    }
  }
}
