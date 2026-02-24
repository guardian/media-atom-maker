package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, KinesisAccess}
import com.gu.media.logging.Logging
import com.gu.media.model._
import com.gu.media.Settings

class PlutoUploadActions(
    config: Settings with DynamoAccess with KinesisAccess
) extends Logging {

  def sendToPluto(plutoIntegrationMessage: PlutoIntegrationMessage): Unit = {
    sendKinesisMessage(plutoIntegrationMessage)
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
