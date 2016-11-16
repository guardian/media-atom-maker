package controllers

import javax.inject._

import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import util.{YouTubeConfig, YouTubeVideoCategoryApi}

class YoutubeCategory @Inject() (val authActions: HMACAuthActions,
                                 val youtubeConfig: YouTubeConfig) extends AtomController {
  import authActions.AuthAction

  def list() = AuthAction {
    val categories = YouTubeVideoCategoryApi(youtubeConfig).list
    Ok(Json.toJson(categories))
  }
}
