package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, KinesisAccess}
import com.gu.media.logging.Logging
import com.gu.media.model._
import com.gu.media.{PlutoDataStore, Settings}

class PlutoUploadActions(
    config: Settings with DynamoAccess with KinesisAccess
) extends Logging {
  private val plutoStore =
    new PlutoDataStore(config.scanamo, config.manualPlutoDynamo)

  def sendToPluto(plutoIntegrationMessage: PlutoIntegrationMessage): Unit = {

    sendKinesisMessage(plutoIntegrationMessage)

    plutoIntegrationMessage match {
      case plutoData: PlutoSyncMetadataMessage if plutoData.projectId.isEmpty =>
        plutoStore.put(plutoData)
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
