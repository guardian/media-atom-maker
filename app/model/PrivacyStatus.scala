package model

import com.gu.contentatom.thrift.atom.media.{PrivacyStatus => ThriftPrivacyStatus}
import play.api.libs.json._

sealed trait PrivacyStatus {
  def name: String
  def asThrift = ThriftPrivacyStatus.valueOf(name)
}


object PrivacyStatus {
  case object Private extends PrivacyStatus { val name = "private"}
  case object Unlisted extends PrivacyStatus { val name = "unlisted"}
  case object Public extends PrivacyStatus { val name = "public"}

  val reads: Reads[PrivacyStatus] = Reads[PrivacyStatus](json => {
    json.as[String] match {
      case "private" => JsSuccess(Private)
      case "unlisted" => JsSuccess(Unlisted)
      case "public" => JsSuccess(Public)
    }
  })

  val writes: Writes[PrivacyStatus] = Writes[PrivacyStatus](p => JsString(p.name))

  implicit val format = Format(reads, writes)

  private val types = List(Private, Unlisted, Public)

  def fromThrift(status: ThriftPrivacyStatus): Option[PrivacyStatus] = types.find(_.name == status.name)
}
