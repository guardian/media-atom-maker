package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class VideoUpload (
                          id: String,
                          title: String,
                          description: String,
                          plutoProjectId: Option[String])
object VideoUpload {

  implicit val videoUploadRead: Reads[VideoUpload] =
    (
      (__ \ "id").read[String] ~
        (__ \ "title").read[String]~
        (__ \ "description").read[String] ~
        (__ \ "plutoProjectId").readNullable[String]
      )(VideoUpload.apply _)

  implicit val videoUploadResponse: Writes[VideoUpload] = (
    (__ \ "id").write[String] ~
      (__ \ "title").write[String]~
      (__ \ "description").write[String] ~
      (__ \ "plutoProjectId").writeNullable[String]
    ) {
    videoUpload: VideoUpload => (
      videoUpload.id,
      videoUpload.title,
      videoUpload.description,
      videoUpload.plutoProjectId
      )
  }

}
