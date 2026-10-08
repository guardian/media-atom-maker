package com.gu.media.model

import play.api.libs.json._

case class MediaAtomPublishSettings(
    atomId: String,
    // When true, this prevents the YouTube title/description from being overwritten on publish
    retainYoutubeFurniture: Boolean = false,
    retainYoutubeAds: Boolean = false
)

object MediaAtomPublishSettings {
  implicit val format: OFormat[MediaAtomPublishSettings] =
    new OFormat[MediaAtomPublishSettings] {
      override def reads(json: JsValue): JsResult[MediaAtomPublishSettings] =
        for {
          atomId <- (json \ "atomId").validate[String]
          retainYoutubeFurniture <- (json \ "retainYoutubeFurniture")
            .validateOpt[Boolean]
            .map(_.getOrElse(false))
          retainYoutubeAds <- (json \ "retainYoutubeAds")
            .validateOpt[Boolean]
            .map(_.getOrElse(false))
        } yield MediaAtomPublishSettings(
          atomId,
          retainYoutubeFurniture,
          retainYoutubeAds
        )

      override def writes(
          settings: MediaAtomPublishSettings
      ): JsObject = Json.obj(
        "atomId" -> settings.atomId,
        "retainYoutubeFurniture" -> settings.retainYoutubeFurniture,
        "retainYoutubeAds" -> settings.retainYoutubeAds
      )
    }
}
