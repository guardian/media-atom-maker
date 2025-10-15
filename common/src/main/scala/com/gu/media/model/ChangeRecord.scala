package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.contentatom.thrift.{ChangeRecord => ThriftChangeRecord}
import com.gu.pandomainauth.model.{User => PandaUser}
import com.gu.ai.x.play.json.Jsonx
import org.joda.time.DateTime
import play.api.libs.json.Format
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

case class ChangeRecord(date: DateTime, user: Option[User]) {
  def asThrift = ThriftChangeRecord(date.getMillis, user.map(_.asThrift))
}

object ChangeRecord {
  implicit val changeRecordFormat: Format[ChangeRecord] =
    Jsonx.formatCaseClassUseDefaults[ChangeRecord]
  def fromThrift(cr: ThriftChangeRecord) =
    ChangeRecord(new DateTime(cr.date), cr.user.map(User.fromThrift))

  def build(date: DateTime, pandaUser: PandaUser): ChangeRecord = {
    // TODO be better
    // HACK: HMAC authenticated users are a `PandaUser` without an email
    // see https://github.com/guardian/media-atom-maker/pull/170
    val user = pandaUser.email match {
      case "" =>
        User(email = pandaUser.firstName, firstName = None, lastName = None)
      case _ =>
        User(
          email = pandaUser.email,
          firstName = Some(pandaUser.firstName),
          lastName = Some(pandaUser.lastName)
        )
    }

    ChangeRecord(date, Some(user))
  }

  def now(pandaUser: PandaUser): ChangeRecord = {
    ChangeRecord.build(DateTime.now(), pandaUser)
  }
}
