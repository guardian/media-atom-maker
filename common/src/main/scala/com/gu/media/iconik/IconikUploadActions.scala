package com.gu.media.iconik

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.gu.media.Settings
import com.gu.media.aws.SQSAccess

class IconikUploadActions(config: Settings with SQSAccess) {
  def send() = {
    config.sqsClient.sendMessage(
      new SendMessageRequest(
        config.iconicQueueUrl,
        "testing"
      )
    )
  }
}
