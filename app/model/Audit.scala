package model

import java.time.Instant

import com.amazonaws.services.dynamodbv2.document.Item
import org.cvogt.play.json.Jsonx
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Json, Writes}
import com.gu.pandomainauth.model.{User => PandaUser}

object Audit {
  implicit val auditFormat: Format[Audit] = Jsonx.formatCaseClass[Audit]

  def fromItem(item: Item): Audit = Json.parse(item.toJSON).as[Audit]

  def apply[T: Writes](atomId: String, operation: String, obj: T, user: PandaUser): Audit = {
    val description = Json.stringify(Json.toJson(obj))
    Audit(atomId, operation, Some(description), Instant.now().toEpochMilli, getUsername(user))
  }

  def getUsername (user: PandaUser): String = {
    user.email match {
      case "" => user.firstName
      case _ => user.email
    }
  }
}

case class Audit(
  atomId: String,
  operation: String,
  description: Option[String],
  date: Long,
  user: String) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())
}
