package model

import play.api.libs.json._

case class UpdatedMetadata(description: Option[String], tags: Option[List[String]], categoryId: Option[String], license: Option[String])

object UpdatedMetadata {
  implicit val metadataRead = Json.reads[UpdatedMetadata]
}

// The license has only this 2 valid values. Update the thrift model?
object VideoLicense {
  val Youtube = "youtube"
  val CreativeCommon = "creativeCommon"
}
