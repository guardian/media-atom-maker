package controllers

import com.gu.pandomainauth.action.AuthActions
import play.api.mvc.{Action, BaseController, ControllerComponents}

class Login(
    val authActions: AuthActions,
    val controllerComponents: ControllerComponents
) extends BaseController {
  import authActions.processOAuthCallback
  import authActions.AuthAction

  def oauthCallback = Action.async { implicit req =>
    processOAuthCallback()
  }

  def reauth = AuthAction {
    Ok("auth ok")
  }
}
