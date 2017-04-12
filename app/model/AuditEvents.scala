package model

import java.time.{Instant, ZoneId}
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.util.Locale

import org.cvogt.play.json.Jsonx
import play.api.libs.json._

import com.gu.pandomainauth.model.{User => PandaUser}

case class AuditEvent(atomId: String, operation: String, description: JsValue, date: Long, user: String)

object AuditEvent {
  implicit val format = Jsonx.formatCaseClass[AuditEvent]

  def create(user: PandaUser, atom: MediaAtom): AuditEvent = {
    val description = JsObject(Seq(
      "title" -> JsString(atom.title),
      "description" -> JsString(atom.description.getOrElse("")),
      "category" -> JsString(atom.category.toString),
      "channel" -> JsString(atom.channelId.getOrElse("none")),
      "privacyStatus" -> JsString(atom.privacyStatus.map(_.toString).getOrElse("none"))
    ))

    AuditEvent(atom.id, "create", description, Instant.now().toEpochMilli, getUsername(user))
  }

  def update(user: PandaUser, before: MediaAtom, after: MediaAtom): AuditEvent = {
    val description = JsObject(Seq(
      diff(before.title, after.title).map("title" -> _),
      diff(before.category, after.category).map("category" -> _),
      diffOpt(before.plutoProjectId, after.plutoProjectId).map("plutoProjectId" -> _),
      diffOpt(before.source, after.source).map("source" -> _),
      diffOpt(before.posterImage.map(_.mediaId), after.posterImage.map(_.mediaId)).map("posterImage" -> _),
      diffOpt(before.description, after.description).map("description" -> _),
      diffSet(before.tags, after.tags).map("tags" -> _),
      diffOpt(before.youtubeCategoryId, after.youtubeCategoryId).map("youtubeCategoryId" -> _),
      diffOpt(before.expiryDate.map(fromEpochMilli), after.expiryDate.map(fromEpochMilli)).map("expiryDate" -> _),
      diffOpt(before.license, after.license).map("license" -> _),
      diffOpt(before.channelId, after.channelId).map("channelId" -> _),
      diff(before.commentsEnabled, after.commentsEnabled).map("commentsEnabled" -> _),
      diffOpt(before.legallySensitive, after.legallySensitive).map("legallySensitive" -> _),
      diffOpt(before.privacyStatus, after.privacyStatus).map("privacyStatus" -> _)
    ).flatten)

    AuditEvent(after.id, "create", description, Instant.now().toEpochMilli, getUsername(user))
  }

  def delete(user: PandaUser, atomId: String): AuditEvent = {
    AuditEvent(atomId, "delete", JsNull, Instant.now().toEpochMilli, getUsername(user))
  }

  def publish(user: PandaUser, atomId: String): AuditEvent = {
    AuditEvent(atomId, "publish", JsNull, Instant.now().toEpochMilli, getUsername(user))
  }

  def addAsset(user: PandaUser, atomId: String, asset: Asset, source: String): AuditEvent = {
    val description = JsObject(Seq(
      "asset" -> Json.toJson(asset),
      "source" -> JsString(source)
    ))

    AuditEvent(atomId, "add_asset", description, Instant.now().toEpochMilli, getUsername(user))
  }

  def activateAsset(user: PandaUser, atomId: String, asset: Asset): AuditEvent = {
    val description = JsObject(Seq(
      "asset" -> Json.toJson(asset)
    ))

    AuditEvent(atomId, "activate_asset", description, Instant.now().toEpochMilli, getUsername(user))
  }

  // TODO MRB: generic way of doing this?
  def diff[T](before: T, after: T): Option[JsValue] = {
    if(before == after) {
      None
    } else {
      Some(JsObject(Seq(
        "before" -> JsString(before.toString),
        "after" -> JsString(after.toString)
      )))
    }
  }

  def diffOpt[T](before: Option[T], after: Option[T]): Option[JsValue] = {
    (before, after) match {
      case (Some(value), None) =>
        Some(JsNull)

      case (None, Some(value)) =>
        Some(JsString(value.toString))

      case (Some(b), Some(a)) =>
        diff(b, a)

      case _ =>
        None
    }
  }

  def diffSet[T](before: Iterable[T], after: Iterable[T]): Option[JsValue] = {
    val setBefore = before.toSet
    val setAfter = after.toSet

    val added = setAfter.diff(setBefore)
    val removed = setBefore.diff(setAfter)

    def jsList(data: Iterable[T]) = JsArray(data.map(_.toString).map(JsString).toList)

    if(added.nonEmpty && removed.nonEmpty) {
      Some(JsObject(Seq(
        "added" -> jsList(added),
        "removed" -> jsList(removed)
      )))
    } else if(added.nonEmpty) {
      Some(JsObject(Seq(
        "added" -> jsList(added)
      )))
    } else if(removed.nonEmpty) {
      Some(JsObject(Seq(
        "removed" -> jsList(added)
      )))
    } else {
      None
    }
  }

  def fromEpochMilli(ts: Long): String = {
    val formatter = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.SHORT)
      .withLocale(Locale.UK)
      .withZone(ZoneId.of("UTC"))

    val instant = Instant.ofEpochMilli(ts)

    formatter.format(instant)
  }

  def getUsername (user: PandaUser): String = {
    user.email match {
      case "" => user.firstName
      case _ => user.email
    }
  }
}
