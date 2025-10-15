package com.gu.media.model

import com.gu.contentatom.thrift.{
  ContentChangeDetails => ThriftContentChangeDetails
}
import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.OFormat

case class ContentChangeDetails(
    lastModified: Option[ChangeRecord],
    created: Option[ChangeRecord],
    published: Option[ChangeRecord],
    revision: Long,
    scheduledLaunch: Option[ChangeRecord],
    embargo: Option[ChangeRecord],
    expiry: Option[ChangeRecord]
) {
  def asThrift = ThriftContentChangeDetails(
    lastModified.map(_.asThrift),
    created.map(_.asThrift),
    published.map(_.asThrift),
    revision,
    scheduledLaunch = scheduledLaunch.map(_.asThrift),
    embargo = embargo.map(_.asThrift),
    expiry = expiry.map(_.asThrift)
  )
}

object ContentChangeDetails {
  implicit val contentChangeDetailsFormat: OFormat[ContentChangeDetails] =
    Jsonx.formatCaseClass[ContentChangeDetails]

  def fromThrift(ccd: ThriftContentChangeDetails) = ContentChangeDetails(
    ccd.lastModified.map(ChangeRecord.fromThrift),
    ccd.created.map(ChangeRecord.fromThrift),
    ccd.published.map(ChangeRecord.fromThrift),
    ccd.revision,
    ccd.scheduledLaunch.map(ChangeRecord.fromThrift),
    ccd.embargo.map(ChangeRecord.fromThrift),
    ccd.expiry.map(ChangeRecord.fromThrift)
  )
}
