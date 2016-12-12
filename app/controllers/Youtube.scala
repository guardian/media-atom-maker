package controllers

import javax.inject._

import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import util._

class Youtube @Inject() (val authActions: HMACAuthActions,
                                 val youtubeConfig: YouTubeConfig) extends AtomController {
  import authActions.AuthAction

  def listCategories() = AuthAction {
    val categories = YouTubeVideoCategoryApi(youtubeConfig).list
    Ok(Json.toJson(categories))
  }

  def listChannels() = AuthAction {
    val channels = YouTubeChannelsApi(youtubeConfig).fetchMyChannels()
    Ok(Json.toJson(channels))
  }
}
