package util

import org.cvogt.play.json.Jsonx
import play.api.libs.json._

case class ActivateAssetRequest (
  atomId: String,
  version: Long
)

object ActivateAssetRequest {
  implicit val format: Format[ActivateAssetRequest] = Jsonx.formatCaseClass[ActivateAssetRequest]
}
