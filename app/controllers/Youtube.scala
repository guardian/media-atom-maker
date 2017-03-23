package controllers

import com.gu.media.youtube.{YouTube, YouTubeChannel, YouTubeVideoCategory}
import com.gu.pandahmac.HMACAuthActions
import play.api.cache._
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.duration._

class Youtube (val authActions: HMACAuthActions, youTube: YouTube, cache: CacheApi) extends Controller {
  import authActions.AuthAction

  def listCategories() = AuthAction {
    val categories = cache.getOrElse[List[YouTubeVideoCategory]]("categories", 1.hours) {
      youTube.categories}
    Ok(Json.toJson(categories))
  }

  def listChannels() = AuthAction {
    val channels = cache.getOrElse[List[YouTubeChannel]]("channels", 1.hours) {
      youTube.channels}
    Ok(Json.toJson(channels))
  }

  def processingStatus(csvIds: String) = AuthAction {
    val videoIds = csvIds.split(",").map(_.trim).toList
    val response = youTube.getProcessingStatus(videoIds)

    Ok(Json.toJson(response))
  }
}
