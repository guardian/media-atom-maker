package model

import com.gu.contentatom.thrift.{ContentChangeDetails => ThriftContentChangeDetails}
import org.cvogt.play.json.Jsonx

case class ContentChangeDetails(lastModified: Option[ChangeRecord], created: Option[ChangeRecord], published: Option[ChangeRecord], revision: Long) {
  def asThrift = ThriftContentChangeDetails(lastModified.map(_.asThrift), created.map(_.asThrift), published.map(_.asThrift), revision)
}

object ContentChangeDetails {
  implicit val contentChangeDetailsFormat = Jsonx.formatCaseClass[ContentChangeDetails]

  def fromThrift(ccd: ThriftContentChangeDetails) = ContentChangeDetails(
    ccd.lastModified.map(ChangeRecord.fromThrift),
    ccd.created.map(ChangeRecord.fromThrift),
    ccd.published.map(ChangeRecord.fromThrift),
    ccd.revision)
}