package com.gu.media.iconik

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import org.scanamo.DynamoFormat
import org.scanamo.generic.semiauto._
import play.api.libs.json._

abstract class IconikItem {
  val id: String
  val title: String
}

trait WithParentId {
  val parentId: String
}

trait IconikItemWithParentId extends IconikItem with WithParentId

case class IconikWorkingGroup(
    id: String,
    title: String
) extends IconikItem

object IconikWorkingGroup {
  implicit val format: Format[IconikWorkingGroup] =
    Jsonx.formatCaseClass[IconikWorkingGroup]

  implicit def dynamoFormat: DynamoFormat[IconikWorkingGroup] =
    deriveDynamoFormat

  def fromUpsertRequest(
      req: IconikUpsertRequest
  ): IconikWorkingGroup = {
    IconikWorkingGroup(
      id = req.workingGroupId,
      title = req.workingGroupTitle
    )
  }
}

case class IconikCommission(
    workingGroupId: String,
    id: String,
    title: String
) extends IconikItemWithParentId {
  val parentId: String = workingGroupId
}

object IconikCommission {
  implicit val format: Format[IconikCommission] =
    Jsonx.formatCaseClass[IconikCommission]

  implicit def dynamoFormat: DynamoFormat[IconikCommission] =
    deriveDynamoFormat

  def fromUpsertRequest(req: IconikUpsertRequest): IconikCommission = {
    IconikCommission(
      id = req.commissionId,
      title = req.commissionTitle,
      workingGroupId = req.workingGroupId
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
) extends IconikItemWithParentId {
  val parentId: String = commissionId
}

object IconikProject {
  implicit val format: Format[IconikProject] =
    Jsonx.formatCaseClass[IconikProject]

  implicit def dynamoFormat: DynamoFormat[IconikProject] =
    deriveDynamoFormat

  def fromUpsertRequest(req: IconikUpsertRequest): IconikProject =
    IconikProject(
      id = req.id,
      title = req.title,
      status = req.status,
      workingGroupId = req.workingGroupId,
      commissionId = req.commissionId,
      masterPlaceholderId = req.masterPlaceholderId
    )
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
