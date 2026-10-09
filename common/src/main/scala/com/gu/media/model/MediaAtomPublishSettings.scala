package com.gu.media.model

import play.api.libs.json.{Json, OFormat}

case class MediaAtomPublishSettings(
    atomId: String,
    // When true, this prevents the YouTube title/description from being overwritten on publish
    retainYoutubeFurniture: Boolean = false
)

object MediaAtomPublishSettings {
  implicit val format: OFormat[MediaAtomPublishSettings] =
    Json.format[MediaAtomPublishSettings]
}
