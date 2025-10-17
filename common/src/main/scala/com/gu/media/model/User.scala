package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.contentatom.thrift.{User => ThriftUser}
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class User(
    email: String,
    firstName: Option[String],
    lastName: Option[String]
) {
  def asThrift = ThriftUser(email, firstName, lastName)
}

object User {
  implicit val userFormat: Format[User] = Jsonx.formatCaseClassUseDefaults[User]
  def fromThrift(user: ThriftUser) =
    User(user.email, user.firstName, user.lastName)
}
