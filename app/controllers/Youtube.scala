package controllers

import com.gu.media.youtube.YouTubeChannel
import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}
import util.{TrainingMode, YouTube}
import model.commands.CommandExceptions._
import com.gu.media.MediaAtomMakerPermissionsProvider

import scala.concurrent.ExecutionContext.Implicits.global

class Youtube(
    val authActions: HMACAuthActions,
    youtube: YouTube,
    permissionsProvider: MediaAtomMakerPermissionsProvider,
    val controllerComponents: ControllerComponents
) extends BaseController
    with TrainingMode {
  import authActions.AuthAction

  def listCategories() = AuthAction {
    Ok(Json.toJson(youtube.categories))
  }

  def listChannels() = AuthAction { req =>
    val user = req.user

    val hasMakePublicPermission = permissionsProvider
      .getStatusPermissions(user)
      .setVideosOnAllChannelsPublic

    val requiredChannels =
      if (isInTrainingMode(req)) youtube.trainingChannels
      else youtube.allChannels

    val channels = youtube
      .channelsWithData(hasMakePublicPermission)
      .filter(c => requiredChannels.contains(c.id))

    Ok(Json.toJson(channels))
  }

  def commercialVideoInfo(id: String) = AuthAction {
    try {
      Ok(Json.toJson(youtube.getCommercialVideoInfo(id)))
    } catch {
      commandExceptionAsResult
    }
  }
}
