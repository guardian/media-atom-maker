package controllers

import data.DataStores
import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.Controller

class YoutubeTagController(val authActions: HMACAuthActions) extends Controller {

  import authActions.APIHMACAuthAction

  def getById(firstPart: String, secondPart: String) = APIHMACAuthAction { implicit req =>
    Ok(Json.toJson(firstPart + "/" + secondPart))
  }

}
