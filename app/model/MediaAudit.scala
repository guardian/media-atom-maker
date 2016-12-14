package model

import org.cvogt.play.json.Jsonx

case class MediaAudit(
  atomId: String,
  operation: String,
  createdAt: Long,
  user: String,
  description: String) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())

}

object MediaAudit {
  implicit val mediaAuditFormat = Jsonx.formatCaseClass[MediaAudit]

  def fromItem(item: Item) = Json.parse(item.toJSON)
}
