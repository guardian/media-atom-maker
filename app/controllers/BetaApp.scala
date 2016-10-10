package controllers

import javax.inject.Inject

import com.gu.pandomainauth.action.AuthActions

class BetaApp @Inject() (val authActions: AuthActions)
  extends AtomController {

  import authActions.{ AuthAction }

  def index(id: String) = AuthAction { req =>
    Ok(views.html.BetaApp.app("Media Atom Maker"))
  }
}
