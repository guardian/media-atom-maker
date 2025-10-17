package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.contentatom.thrift.atom.media.{PlutoData => ThriftPlutoData}
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class PlutoData(
    commissionId: Option[String],
    projectId: Option[String],
    masterId: Option[String]
) {
  def asThrift: ThriftPlutoData =
    ThriftPlutoData(commissionId, projectId, masterId)
}

object PlutoData {
  implicit val plutoDataFormat: Format[PlutoData] =
    Jsonx.formatCaseClass[PlutoData]

  def fromThrift(plutoData: ThriftPlutoData) = PlutoData(
    plutoData.commissionId,
    plutoData.projectId,
    plutoData.masterId
  )
}
