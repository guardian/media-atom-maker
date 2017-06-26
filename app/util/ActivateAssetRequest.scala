package util

import play.api.libs.json._

sealed abstract class ActivateAssetRequest
case class ActivateAssetByVersion(version: Long) extends ActivateAssetRequest
case class ActivateYouTubeAssetById(id: String) extends ActivateAssetRequest

object ActivateAssetRequest {
  implicit val reads: Reads[ActivateAssetRequest] = Reads(apply)

  def apply(json: JsValue): JsResult[ActivateAssetRequest] = for {
    version <- (json \ "version").validateOpt[Long]
    youTubeId <- (json \ "youtubeId").validateOpt[String]
    request <- parse(version, youTubeId)
  } yield {
    request
  }

  private def parse(version: Option[Long], youTubeId: Option[String]): JsResult[ActivateAssetRequest] = {
    val versionRequest = version.map(ActivateAssetByVersion)
    val youTubeIdRequest = youTubeId.map(ActivateYouTubeAssetById)

    versionRequest orElse youTubeIdRequest match {
      case Some(request) => JsSuccess(request)
      case None => JsError("Missing version or youTubeId")
    }
  }
}
