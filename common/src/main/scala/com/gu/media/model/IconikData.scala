package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import com.gu.contentatom.thrift.atom.media.{IconikData => ThriftIconikData}
import play.api.libs.json.Format

case class IconikData(
    workingGroupId: Option[String],
    commissionId: Option[String],
    projectId: Option[String],
    masterPlaceholderId: Option[String]
) {
  def asThrift: ThriftIconikData =
    ThriftIconikData(
      workingGroupId = workingGroupId,
      commissionId = commissionId,
      projectId = projectId,
      masterPlaceholderId = masterPlaceholderId
    )
}

object IconikData {
  implicit val iconikDataFormat: Format[IconikData] =
    Jsonx.formatCaseClass[IconikData]

  def fromThrift(iconikData: ThriftIconikData) = IconikData(
    iconikData.workingGroupId,
    iconikData.commissionId,
    iconikData.projectId,
    iconikData.masterPlaceholderId
  )
}
