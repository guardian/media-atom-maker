package com.gu.media.pluto

import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.gu.media.util.JsonDate._

case class PlutoProject (
  id: String,
  collectionId: String,
  headline: String,
  productionOffice: String,
  status: String,
  created: DateTime
)

object PlutoProject {
  implicit val plutoProjectReads: Reads[PlutoProject] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "collectionId").read[String] and
    (JsPath \ "headline").read[String] and
    (JsPath \ "productionOffice").read[String] and
    (JsPath \ "status").read[String] and
    (JsPath \ "created").read[String].map(DateTime.parse)
  )(PlutoProject.apply _)

  implicit val plutoProjectWrites: Writes[PlutoProject] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "collectionId").write[String] and
    (JsPath \ "headline").write[String] and
    (JsPath \ "productionOffice").write[String] and
    (JsPath \ "status").write[String] and
    (JsPath \ "created").write[String].contramap((_: DateTime).toString)
  )(unlift(PlutoProject.unapply))


  implicit val format: Format[PlutoProject] = Jsonx.formatCaseClass[PlutoProject]
}
