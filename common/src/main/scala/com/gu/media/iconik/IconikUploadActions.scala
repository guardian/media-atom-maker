package com.gu.media.iconik

import com.amazonaws.services.sqs.model.SendMessageRequest
import com.gu.media.Settings
import com.gu.media.aws.SQSAccess
import com.gu.media.model.{Asset, IconikData, MediaAtom}
import play.api.libs.json.{Json, OFormat}
import com.gu.media.logging.Logging

sealed trait IconikMessage {
  def atomId: String
  def toJsonString: String
}

case class IconikProjectAssignedToAtomMessage(
    atomId: String,
    assets: List[Asset],
    iconikData: IconikData
) extends IconikMessage {
  override def toJsonString: String =
    Json.stringify(Json.toJson(this))
}

object IconikProjectAssignedToAtomMessage {
  implicit val format: OFormat[IconikProjectAssignedToAtomMessage] =
    Json.format[IconikProjectAssignedToAtomMessage]
}

case class AssetUploadedToAtomMessage(
    atomId: String,
    uploadS3Key: String,
    iconikData: Option[IconikData]
) extends IconikMessage {
  override def toJsonString: String =
    Json.stringify(Json.toJson(this))
}

object AssetUploadedToAtomMessage {
  implicit val format: OFormat[AssetUploadedToAtomMessage] =
    Json.format[AssetUploadedToAtomMessage]
}

class IconikUploadActions(config: Settings with SQSAccess) extends Logging {
  def sendMessage(message: IconikMessage) = {
    val payload = message.toJsonString
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
