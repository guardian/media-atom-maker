package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class VideoUpload (
                          id: String,
                          title: String,
                          s3Key: String,
                          plutoProjectId: Option[String])
object VideoUpload {

  implicit val videoUploadRead: Reads[VideoUpload] =
    (
      (__ \ "id").read[String] ~
        (__ \ "title").read[String]~
        (__ \ "s3Key").read[String] ~
        (__ \ "plutoProjectId").readNullable[String]
      )(VideoUpload.apply _)

  implicit val videoUploadResponse: Writes[VideoUpload] = (
    (__ \ "id").write[String] ~
      (__ \ "title").write[String]~
      (__ \ "s3Key").write[String] ~
      (__ \ "plutoProjectId").writeNullable[String]
    ) {
    videoUpload: VideoUpload => (
      videoUpload.id,
      videoUpload.title,
      videoUpload.s3Key,
      videoUpload.plutoProjectId
      )
  }

}
