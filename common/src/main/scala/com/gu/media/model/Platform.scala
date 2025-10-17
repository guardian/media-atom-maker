package com.gu.media.model

import com.gu.contentatom.thrift.atom.media.{Platform => ThriftPlatform}
import play.api.libs.json._

sealed trait Platform {
  def name: String
  def asThrift = ThriftPlatform.valueOf(name).get
}

object Platform {
  case object Youtube extends Platform { val name = "Youtube" }
  case object Facebook extends Platform { val name = "Facebook" }
  case object Dailymotion extends Platform { val name = "Dailymotion" }
  case object Mainstream extends Platform { val name = "Mainstream" }
  case object Url extends Platform { val name = "Url" }

  val platformReads = Reads[Platform](json => {
    json.as[String] match {
      case "Youtube"     => JsSuccess(Youtube)
      case "Facebook"    => JsSuccess(Facebook)
      case "Dailymotion" => JsSuccess(Dailymotion)
      case "Mainstream"  => JsSuccess(Mainstream)
      case "Url"         => JsSuccess(Url)
    }
  })

  val platformWrites: Writes[Platform] = Writes[Platform](cat => {
    JsString(cat.name)
  })

  implicit val platformFormat: Format[Platform] =
    Format(platformReads, platformWrites)

  private val types = List(Youtube, Facebook, Dailymotion, Mainstream, Url)

  def fromThrift(p: ThriftPlatform) = types.find(_.name == p.name).get
}
