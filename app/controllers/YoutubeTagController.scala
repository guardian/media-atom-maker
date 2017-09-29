package controllers

import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.Controller
import com.gu.media.youtube.contentBundlingMap

class YoutubeTagController(val authActions: HMACAuthActions) extends Controller {

  import authActions.APIAuthAction

  def getById(id: String) = APIAuthAction { implicit req =>

    val parts = id.split("/").toList.reverse

    val bundleMatch = parts.find(part => contentBundlingMap.isDefinedAt(part)).getOrElse("")

    Ok(Json.toJson(bundleMatch))

  }
}
