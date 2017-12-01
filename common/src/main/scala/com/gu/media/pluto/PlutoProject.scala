package com.gu.media.pluto

import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.json._
import com.gu.media.util.JsonDate._

case class PlutoCommission (
  id: String,
  title: String
)

object PlutoCommission {
  implicit val format: Format[PlutoCommission] = Jsonx.formatCaseClass[PlutoCommission]

  def build(plutoUpsertRequest: PlutoUpsertRequest): PlutoCommission = {
    PlutoCommission(
      id = plutoUpsertRequest.commissionId,
      title = plutoUpsertRequest.commissionTitle
    )
  }
}

case class PlutoProject (
  id: String,
  title: String,
  status: String,
  commissionId: String,
  commissionTitle: String, //TODO remove this once migrated
  productionOffice: String,
  created: DateTime
)

object PlutoProject {
  implicit val format: Format[PlutoProject] = Jsonx.formatCaseClass[PlutoProject]

  def build(plutoUpsertRequest: PlutoUpsertRequest): PlutoProject = {
    PlutoProject(
      id = plutoUpsertRequest.id,
      title = plutoUpsertRequest.title,
      status = plutoUpsertRequest.status,
      commissionId = plutoUpsertRequest.commissionId,
      commissionTitle = plutoUpsertRequest.commissionTitle,
      productionOffice = plutoUpsertRequest.productionOffice,
      created = plutoUpsertRequest.created
    )
  }
}

// This represents the payload the `pluto-message-ingestion` lambda sends,
// it is the same as what Pluto sends on Kinesis
case class PlutoUpsertRequest (
  id: String,
  title: String,
  status: String,
  productionOffice: String,
  created: DateTime,
  commissionId: String,
  commissionTitle: String
)

object PlutoUpsertRequest {
  implicit val format: Format[PlutoUpsertRequest] = Jsonx.formatCaseClass[PlutoUpsertRequest]
}
