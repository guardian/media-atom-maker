package com.gu.media.pluto

import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.json._
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
  implicit val format: Format[PlutoProject] = Jsonx.formatCaseClass[PlutoProject]
}
