package com.gu.media.model

import com.gu.contentatom.thrift.{ChangeRecord => ThriftChangeRecord}
import com.gu.pandomainauth.model.{User => PandaUser}
import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime
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
      case "" => User(email = pandaUser.firstName,
        firstName = None,
        lastName = None
      )
      case _ => User(
        email = pandaUser.email,
        firstName = Some(pandaUser.firstName),
        lastName = Some(pandaUser.lastName)
      )
    }

    ChangeRecord(DateTime.now(), Some(user))
  }
}
