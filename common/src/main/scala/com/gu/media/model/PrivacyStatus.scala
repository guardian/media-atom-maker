package com.gu.media.model

import com.gu.contentatom.thrift.atom.media.{
  PrivacyStatus => ThriftPrivacyStatus
}
import play.api.libs.json._

sealed trait PrivacyStatus {
  def name: String
  def asThrift = ThriftPrivacyStatus.valueOf(name)
}

object PrivacyStatus {
  case object Private extends PrivacyStatus { val name = "Private" }
  case object Unlisted extends PrivacyStatus { val name = "Unlisted" }
  case object Public extends PrivacyStatus { val name = "Public" }

  val reads: Reads[PrivacyStatus] = Reads[PrivacyStatus](json => {
    json.as[String] match {
      case "Private"  => JsSuccess(Private)
      case "Unlisted" => JsSuccess(Unlisted)
      case "Public"   => JsSuccess(Public)
    }
  })

  val writes: Writes[PrivacyStatus] =
    Writes[PrivacyStatus](p => JsString(p.name))

  implicit val format: Format[PrivacyStatus] = Format(reads, writes)

  val all: Set[PrivacyStatus] = Set(Private, Unlisted, Public)

  def fromThrift(status: ThriftPrivacyStatus): Option[PrivacyStatus] =
    all.find(_.name == status.name)
}
