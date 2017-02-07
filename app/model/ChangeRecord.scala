package model

import com.gu.contentatom.thrift.{ChangeRecord => ThriftChangeRecord}
import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime
import com.gu.pandomainauth.model.{User => PandaUser}
import play.api.libs.json.Format

case class ChangeRecord(date: DateTime, user: Option[User]) {
  def asThrift = ThriftChangeRecord(date.getMillis, user.map(_.asThrift))
}

object ChangeRecord {
  implicit val changeRecordFormat: Format[ChangeRecord] = Jsonx.formatCaseClassUseDefaults[ChangeRecord]
  def fromThrift(cr: ThriftChangeRecord) = ChangeRecord(new DateTime(cr.date), cr.user.map(User.fromThrift))

  // TODO be better
  // HACK: HMAC authenticated users are a `PandaUser` without an email
  // see https://github.com/guardian/media-atom-maker/pull/170
  def now (pandaUser: PandaUser): ChangeRecord = {
    val user = pandaUser.email match {
      case "" => User(pandaUser.firstName, Some("bot"), Some(pandaUser.firstName))
      case _ => User(pandaUser.firstName, Some(pandaUser.lastName), Some(pandaUser.email))
    }

    ChangeRecord(DateTime.now(), Some(user))
  }
}
