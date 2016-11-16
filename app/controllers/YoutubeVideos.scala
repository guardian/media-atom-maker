package controllers

import javax.inject._

import com.gu.pandahmac.HMACAuthActions
import util.{YouTubeVideoUpdateApi, YouTubeConfig}

class YoutubeVideos @Inject()(val authActions: HMACAuthActions,
                              val youtubeConfig: YouTubeConfig) extends AtomController {
  import authActions.AuthAction

  def update(atomId: String) = AuthAction { implicit req =>
    req.body.asJson.map { json =>
      YouTubeVideoUpdateApi(youtubeConfig).updateMetadata(atomId, json) match {
        case Some(video) => Ok
        case None => NotFound
      }
    }.getOrElse {
      BadRequest("Could not read json")
    }
  }
}
