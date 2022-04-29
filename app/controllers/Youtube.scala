package controllers

import com.gu.media.youtube.YouTubeChannel
import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}
import util.{TrainingMode, YouTube}
import model.commands.CommandExceptions._
import com.gu.media.MediaAtomMakerPermissionsProvider

import scala.concurrent.ExecutionContext.Implicits.global

class Youtube (val authActions: HMACAuthActions, youtube: YouTube, permissions: MediaAtomMakerPermissionsProvider, val controllerComponents: ControllerComponents)
  extends BaseController with TrainingMode {
  import authActions.AuthAction

  def listCategories() = AuthAction {
    Ok(Json.toJson(youtube.categories))
  }

  def listChannels() = AuthAction.async { req =>
    val isTrainingMode = isInTrainingMode(req)
    val user = req.user

    permissions.getStatusPermissions(user).map(permissions => {
      val hasMakePublicPermission = permissions.setVideosOnAllChannelsPublic

      val channels = if (isTrainingMode) {
        youtube.channelsWithData(hasMakePublicPermission).filter(c => youtube.trainingChannels.contains(c.id))
      } else {
        youtube.channelsWithData(hasMakePublicPermission).filter(c => youtube.allChannels.contains(c.id))
      }

      Ok(Json.toJson(channels))
    })
  }

  def commercialVideoInfo(id: String) = AuthAction {
    try {
      Ok(Json.toJson(youtube.getCommercialVideoInfo(id)))
    } catch {
      commandExceptionAsResult
    }
  }
}
