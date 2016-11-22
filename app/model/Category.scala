package model

import com.gu.contentatom.thrift.atom.media.{Category => ThriftCategory}
import play.api.libs.json._

sealed trait Category {
  def name: String

  def asThrift = ThriftCategory.valueOf(name).get
}

object Category {
  case object Documentary extends Category { val name = "Documentary" }
  case object Explainer extends Category { val name = "Explainer" }
  case object Feature extends Category { val name = "Feature" }
  case object Hosted extends Category { val name = "Hosted" }
  case object News extends Category { val name = "News" }

  val categoryReads = Reads[Category](json => {
    json.as[String] match {
      case "Documentary" => JsSuccess(Documentary)
      case "Explainer" => JsSuccess(Documentary)
      case "Feature" => JsSuccess(Feature)
      case "Hosted" => JsSuccess(Hosted)
      case "News" => JsSuccess(News)
    }
  })

  val categoryWrites = Writes[Category] (cat => {
    JsString(cat.name)
  })

  implicit val categoryFormat = Format(categoryReads, categoryWrites)

  private val types = List(Documentary, Explainer, Feature, Hosted, News)

  def fromThrift(cat: ThriftCategory) = types.find(_.name == cat.name).get
}