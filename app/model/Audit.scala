package model

import com.amazonaws.services.dynamodbv2.document.Item
import org.cvogt.play.json.Jsonx
import play.api.libs.functional.syntax._
import play.api.libs.json.Json

case class Audit(
  atomId: String,
  operation: String,
  description: Option[String],
  date: Long,
  user: String) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object Audit {
  implicit val auditFormat = Jsonx.formatCaseClass[Audit]

  def fromItem(item: Item) = Json.parse(item.toJSON).as[Audit]
}
