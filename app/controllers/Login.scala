package controllers

import com.gu.pandomainauth.action.AuthActions
import play.api.mvc.{Action, BaseController, ControllerComponents}

class Login(val authActions: AuthActions, val controllerComponents: ControllerComponents) extends BaseController {
  import authActions.processGoogleCallback
  import authActions.AuthAction

  def oauthCallback = Action.async { implicit req =>
    processGoogleCallback()
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }
}
