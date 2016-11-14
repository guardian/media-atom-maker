package model

import com.gu.contentatom.thrift.atom.media.{Platform => ThriftPlatform}
import play.api.libs.json._

sealed trait Platform {
  def name: String
  def asThrift = ThriftPlatform.valueOf(name).get
}

object Platform {
  case object YOUTUBE extends Platform { val name = "YOUTUBE" }
  case object FACEBOOK extends Platform { val name = "FACEBOOK" }
  case object DAILYMOTION extends Platform { val name = "DAILYMOTION" }
  case object MAINSTREAM extends Platform { val name = "MAINSTREAM" }
  case object URL extends Platform { val name = "URL" }

  val platformReads = Reads[Platform](json => {
    json.as[String] match {
      case "YOUTUBE" => JsSuccess(YOUTUBE)
      case "FACEBOOK" => JsSuccess(FACEBOOK)
      case "DAILYMOTION" => JsSuccess(DAILYMOTION)
      case "MAINSTREAM" => JsSuccess(MAINSTREAM)
      case "URL" => JsSuccess(URL)
    }
  })

  val platformWrites = Writes[Platform] (cat => {
    JsString(cat.name)
  })

  implicit val platformFormat = Format(platformReads, platformWrites)

  private val types = List(YOUTUBE, FACEBOOK, DAILYMOTION, MAINSTREAM, URL)

  def fromThrift(p: ThriftPlatform) = types.find(_.name == p.name).get
}