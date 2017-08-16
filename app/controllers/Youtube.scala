package controllers

import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.Controller
import util.{TrainingMode, YouTube}
import model.commands.CommandExceptions._

class Youtube (val authActions: HMACAuthActions, youtube: YouTube) extends Controller with TrainingMode {
  import authActions.AuthAction

  def listCategories() = AuthAction {
    Ok(Json.toJson(youtube.categories))
  }

  def listChannels() = AuthAction { req =>
    val isTrainingMode = isInTrainingMode(req)

    val channels = if (isTrainingMode) {
      youtube.trainingChannels match {
        case Nil => List()
        case trainingList => youtube.channels.filter(c => trainingList.contains(c.id))
      }
    } else {
      youtube.allowedChannels match {
        case Nil => youtube.channels
        case allowedList => youtube.channels.filter(c => allowedList.contains(c.id))
      }
    }

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
