package com.gu.media

import com.amazonaws.services.dynamodbv2.document.Item
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Json

object Audit {
  implicit val auditFormat = Jsonx.formatCaseClass[Audit]

  def fromItem(item: Item) = Json.parse(item.toJSON).as[Audit]
}

case class Audit(
  atomId: String,
  operation: String,
  description: Option[String],
  date: Long,
  user: String) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())
}
