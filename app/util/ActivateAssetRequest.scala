package util

import ai.x.play.json.Encoders._
import ai.x.play.json.Jsonx
import play.api.libs.json._

case class ActivateAssetRequest (
  atomId: String,
  version: Long
)

object ActivateAssetRequest {
  implicit val format: Format[ActivateAssetRequest] = Jsonx.formatCaseClass[ActivateAssetRequest]
}
