package controllers

import javax.inject._
import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}

class Transcoder(
    val authActions: HMACAuthActions,
    transcoder: util.Transcoder,
    val controllerComponents: ControllerComponents
) extends BaseController {
  import authActions.AuthAction

  def jobStatus = AuthAction {
    Ok(Json.toJson(transcoder.getJobsStatus))
  }

}
