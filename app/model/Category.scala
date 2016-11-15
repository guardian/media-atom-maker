package model

import com.gu.contentatom.thrift.atom.media.{Category => ThriftCategory}
import play.api.libs.json._

sealed trait Category {
  def name: String

  def asThrift = ThriftCategory.valueOf(name).get
}

object Category {
  case object DOCUMENTARY extends Category { val name = "DOCUMENTARY" }
  case object EXPLAINER extends Category { val name = "EXPLAINER" }
  case object FEATURE extends Category { val name = "FEATURE" }
  case object HOSTED extends Category { val name = "HOSTED" }
  case object NEWS extends Category { val name = "NEWS" }

  val categoryReads = Reads[Category](json => {
    json.as[String] match {
      case "DOCUMENTARY" => JsSuccess(DOCUMENTARY)
      case "EXPLAINER" => JsSuccess(DOCUMENTARY)
      case "FEATURE" => JsSuccess(FEATURE)
      case "HOSTED" => JsSuccess(HOSTED)
      case "NEWS" => JsSuccess(NEWS)
    }
  })

  val categoryWrites = Writes[Category] (cat => {
    JsString(cat.name)
  })

  implicit val categoryFormat = Format(categoryReads, categoryWrites)

  private val types = List(DOCUMENTARY, EXPLAINER, FEATURE, HOSTED, NEWS)

  def fromThrift(cat: ThriftCategory) = types.find(_.name == cat.name).get
}