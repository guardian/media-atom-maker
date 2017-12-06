package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, KinesisAccess, SESSettings}
import com.gu.media.logging.Logging
import com.gu.media.model._
import com.gu.media.ses.Mailer
import com.gu.media.{PlutoDataStore, Settings}

class PlutoUploadActions(config: Settings with DynamoAccess with KinesisAccess with SESSettings) extends Logging {
  private val mailer = new Mailer(config.sesClient, config.getMandatoryString("host"))
  private val plutoStore = new PlutoDataStore(config.dynamoDB, config.manualPlutoDynamo)

  def sendToPluto(plutoIntegrationData: PlutoIntegrationData): Unit = {
    plutoIntegrationData match {
      case plutoData: PlutoSyncMetadata => {
        plutoData.projectId match {
          case Some(_) => sendKinesisMessage(plutoIntegrationData)
          case None => {
            // TODO MRB: re-enable this once the project selector has been fixed
            //        log.info(s"Sending missing Pluto ID email user=${metadata.user} atom=${plutoData.atomId}")
            //
            //        mailer.sendPlutoIdMissingEmail(
            //          metadata.title,
            //          metadata.user,
            //          config.fromEmailAddress,
            //          config.replyToAddresses)
            //
            //        plutoStore.put(plutoData)
          }
        }
      }
      case _ => sendKinesisMessage(plutoIntegrationData)
    }
  }

  private def sendKinesisMessage(plutoIntegrationData: PlutoIntegrationData): Unit = {
    log.info(s"writing message to pluto integration stream: type=${plutoIntegrationData.`type`} atomId=${plutoIntegrationData.atomId} content=${plutoIntegrationData}")
    config.sendOnKinesis(config.plutoIntegrationOutgoingStream, plutoIntegrationData.partitionKey, plutoIntegrationData)
  }
}
