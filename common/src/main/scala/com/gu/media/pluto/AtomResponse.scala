package com.gu.media.pluto

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class AtomResponse (
                          id: String,
                          title: String,
                          description: String,
                          plutoProjectId: Option[String])
object AtomResponse {

  implicit val atomResponseRead: Reads[AtomResponse] =
    (
      (__ \ "id").read[String] ~
        (__ \ "title").read[String]~
        (__ \ "description").read[String] ~
        (__ \ "plutoProjectId").readNullable[String]
      )(AtomResponse.apply _)

  implicit val atomResponse: Writes[AtomResponse] = (
    (__ \ "id").write[String] ~
      (__ \ "title").write[String]~
      (__ \ "description").write[String] ~
      (__ \ "plutoProjectId").writeNullable[String]
    ) {
    atomResponse: AtomResponse => (
      atomResponse.id,
      atomResponse.title,
      atomResponse.description,
      atomResponse.plutoProjectId
      )
  }

}
