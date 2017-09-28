package controllers

import com.gu.pandomainauth.action.AuthActions
import play.api.mvc.{Action, Controller}

class Login(val authActions: AuthActions) extends Controller {
  import authActions.processGoogleCallback
  import authActions.AuthAction

  def oauthCallback = Action.async { implicit req =>
    processGoogleCallback()
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }
}
