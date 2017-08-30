package controllers

import data.DataStores
import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.Controller
import com.gu.media.youtube.contentBundlingMap
class YoutubeTagController(val authActions: HMACAuthActions) extends Controller {

  import authActions.APIHMACAuthAction

  def getById(firstPart: String, secondPart: String) = APIHMACAuthAction { implicit req =>

    if (contentBundlingMap.isDefinedAt(secondPart)) {
      println("found second")
      Ok(Json.toJson(secondPart))
    }

    else if (contentBundlingMap.isDefinedAt(firstPart)) {
      println("fournd first ")
      Ok(Json.toJson(firstPart))
    }

    else {
      println("found nothing")
      Ok(Json.toJson(""))
    }
  }

}
