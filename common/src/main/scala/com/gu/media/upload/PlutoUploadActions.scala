package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, KinesisAccess, SESSettings}
import com.gu.media.logging.Logging
import com.gu.media.model._
import com.gu.media.ses.Mailer
import com.gu.media.{PlutoDataStore, Settings}

class PlutoUploadActions(config: Settings with DynamoAccess with KinesisAccess with SESSettings) extends Logging {
  private val mailer = new Mailer(config.sesClient, config.getMandatoryString("host"))
  private val plutoStore = new PlutoDataStore(config.dynamoDB, config.manualPlutoDynamo)

  def sendToPluto(plutoIntegrationMessage: PlutoIntegrationMessage): Unit = {
    plutoIntegrationMessage match {
      case plutoData: PlutoSyncMetadataMessage => {
        plutoData.projectId match {
          case Some(_) => sendKinesisMessage(plutoIntegrationMessage)
          case None => {
            plutoStore.put(plutoData)

            val shouldSendEmailReminder = plutoData.user != config.integrationTestUser && config.syncWithPluto

            if (shouldSendEmailReminder) {
              log.info(s"Sending missing Pluto ID email user=${plutoData.user} atom=${plutoData.atomId}")

              mailer.sendPlutoIdMissingEmail(
                plutoData.atomId,
                plutoData.title,
                plutoData.user,
                config.fromEmailAddress,
                config.replyToAddresses
              )
            }
          }
        }
      }
      case _ => sendKinesisMessage(plutoIntegrationMessage)
    }
  }

  private def sendKinesisMessage(plutoIntegrationMessage: PlutoIntegrationMessage): Unit = {
    if (config.syncWithPluto) {
      log.info(s"writing message to pluto integration stream: type=${plutoIntegrationMessage.`type`} atomId=${plutoIntegrationMessage.atomId} content=$plutoIntegrationMessage")
      config.sendOnKinesis(config.plutoIntegrationOutgoingStream, plutoIntegrationMessage.partitionKey, plutoIntegrationMessage)
    }
  }
}
