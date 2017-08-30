package controllers

import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.Controller
import com.gu.media.youtube.contentBundlingMap

class YoutubeTagController(val authActions: HMACAuthActions) extends Controller {

  import authActions.APIHMACAuthAction

  def getById(id: String) = APIHMACAuthAction { implicit req =>

    val parts = id.split("/").toList.reverse

    val bundleMatch = parts.find(part => contentBundlingMap.isDefinedAt(part)).getOrElse("")

    Ok(Json.toJson(bundleMatch))

  }
}
