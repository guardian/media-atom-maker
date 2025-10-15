package com.gu.media.model

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class UpdatedMetadata(
    title: Option[String],
    description: Option[String],
    tags: Option[List[String]],
    categoryId: Option[String],
    license: Option[String],
    privacyStatus: Option[PrivacyStatus],
    expiryDate: Option[Long],
    plutoId: Option[String]
)

object UpdatedMetadata {
  implicit val metadataRead: Reads[UpdatedMetadata] = (
    (__ \ "title").readNullable[String] ~
      (__ \ "description").readNullable[String] ~
      (__ \ "tags").readNullable[List[String]] ~
      (__ \ "categoryId").readNullable[String] ~
      (__ \ "license").readNullable[String] ~
      (__ \ "privacyStatus").readNullable[PrivacyStatus] ~
      (__ \ "expiryDate").readNullable[Long] ~
      (__ \ "plutoId").readNullable[String]
  )(UpdatedMetadata.apply _)
}

// The license has only this 2 valid values. Update the thrift model?
object VideoLicense {
  val Youtube = "youtube"
  val CreativeCommon = "creativeCommon"
}
