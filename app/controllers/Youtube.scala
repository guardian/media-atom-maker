package controllers

import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.Controller
import util.YouTube
import model.commands.CommandExceptions._

class Youtube (val authActions: HMACAuthActions, youTube: YouTube) extends Controller {
  import authActions.AuthAction

  def listCategories() = AuthAction {
    Ok(Json.toJson(youTube.categories))
  }

  def listChannels() = AuthAction {
    val channels = youTube.allowedChannels match {
      case Nil => youTube.channels
      case allowedList => youTube.channels.filter(c => allowedList.contains(c.id))
    }

    Ok(Json.toJson(channels))
  }

  def commercialVideoInfo(id: String) = AuthAction {
    try {
      Ok(Json.toJson(youTube.getCommercialVideoInfo(id)))
    } catch {
      commandExceptionAsResult
    }
  }
}
