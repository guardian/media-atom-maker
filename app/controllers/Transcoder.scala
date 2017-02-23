package controllers

import javax.inject._

import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.Controller

class Transcoder @Inject()(val authActions: HMACAuthActions, transcoder: util.Transcoder) extends Controller {
  import authActions.AuthAction

  def jobStatus = AuthAction {
    Ok(Json.toJson(transcoder.getJobsStatus))
  }

}


