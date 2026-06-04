package model

import play.api.libs.json.{Json, OFormat}

case class CropOption(key: String, ratio: String, ratioString: String)

object CropOption {
  implicit val jf: OFormat[CropOption] = Json.format[CropOption]
}
