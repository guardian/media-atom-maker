package com.gu.media.iconik

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.gu.media.Settings
import com.gu.media.aws.SQSAccess
import com.gu.media.model.{IconikData, MediaAtom}
import play.api.libs.json.{Json, OFormat}
import com.gu.media.logging.Logging

case class IconikUploadMessage(atomId: String, iconikData: Option[IconikData])

object IconikUploadMessage {
  implicit val iconikUploadMessageFormat: OFormat[IconikUploadMessage] =
    Json.format[IconikUploadMessage]
}

class IconikUploadActions(config: Settings with SQSAccess) extends Logging {
  def send(newAtom: MediaAtom) = {
    val payload = Json.stringify(
      Json.toJson(
        IconikUploadMessage(newAtom.id, newAtom.iconikData)
      )
    )
    log.info(
      s"Updating Iconik with latest change for atom: $payload"
    )
    config.sqsClient.sendMessage(
      new SendMessageRequest(
        config.iconikQueueUrl,
        payload
      )
    )
  }
}
