package model

import com.gu.contentatom.thrift.{ChangeRecord => ThriftChangeRecord}
import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime

case class ChangeRecord(date: DateTime, user: Option[User]) {
  def asThrift = ThriftChangeRecord(date.getMillis, user.map(_.asThrift))
}

object ChangeRecord {
  implicit val changeRecordFormat = Jsonx.formatCaseClassUseDefaults[ChangeRecord]
  def fromThrift(cr: ThriftChangeRecord) = ChangeRecord(new DateTime(cr.date), cr.user.map(User.fromThrift))
}