package com.gu.media.iconik

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.gu.media.Settings
import com.gu.media.aws.SQSAccess
import com.gu.media.model.IconikData
import play.api.libs.json.Json
import com.gu.media.logging.Logging

class IconikUploadActions(config: Settings with SQSAccess) extends Logging {
  def send(iconikData: IconikData) = {
    log.info("Updating Iconik with latest change")
    config.sqsClient.sendMessage(
      new SendMessageRequest(
        config.iconikQueueUrl,
        Json.stringify(Json.toJson(iconikData))
      )
    )
  }
}
