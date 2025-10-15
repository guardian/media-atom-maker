package com.gu.media.iconik

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json._

sealed trait IconikItem {
  val id: String
}

case class IconikWorkingGroup(
    id: String,
    title: String
) extends IconikItem

object IconikWorkingGroup {
  implicit val format: Format[IconikWorkingGroup] =
    Jsonx.formatCaseClass[IconikWorkingGroup]

  def build(iconikUpsertRequest: IconikUpsertRequest): IconikWorkingGroup = {
    IconikWorkingGroup(
      id = iconikUpsertRequest.workingGroupId,
      title = iconikUpsertRequest.workingGroupTitle
    )
  }
}

case class IconikCommission(
    workingGroupId: String,
    id: String,
    title: String
) extends IconikItem

object IconikCommission {
  implicit val format: Format[IconikCommission] =
    Jsonx.formatCaseClass[IconikCommission]

  def build(iconikUpsertRequest: IconikUpsertRequest): IconikCommission = {
    IconikCommission(
      id = iconikUpsertRequest.commissionId,
      title = iconikUpsertRequest.commissionTitle,
      workingGroupId = iconikUpsertRequest.workingGroupId
    )
  }
}

case class IconikProject(
    id: String,
    title: String,
    status: String,
    workingGroupId: String,
    commissionId: String,
    masterPlaceholderId: Option[String]
) extends IconikItem

object IconikProject {
  implicit val format: Format[IconikProject] =
    Jsonx.formatCaseClass[IconikProject]

  def build(iconikUpsertRequest: IconikUpsertRequest): IconikProject = {
    IconikProject(
      id = iconikUpsertRequest.id,
      title = iconikUpsertRequest.title,
      status = iconikUpsertRequest.status,
      workingGroupId = iconikUpsertRequest.workingGroupId,
      commissionId = iconikUpsertRequest.commissionId,
      masterPlaceholderId = iconikUpsertRequest.masterPlaceholderId
    )
  }
}

// This represents the payload the `iconik-message-ingestion` lambda sends,
// it is the same as what Iconik sends on Kinesis
case class IconikUpsertRequest(
    id: String,
    title: String,
    status: String,
    commissionId: String,
    commissionTitle: String,
    workingGroupId: String,
    workingGroupTitle: String,
    masterPlaceholderId: Option[String]
//    productionOffice: String, // don't know if we need this in Iconik world?
)

object IconikUpsertRequest {
  implicit val format: Format[IconikUpsertRequest] =
    Jsonx.formatCaseClass[IconikUpsertRequest]
}
