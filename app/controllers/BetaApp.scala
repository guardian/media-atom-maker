package controllers

import play.api.mvc._
import javax.inject._
import com.gu.pandomainauth.action.AuthActions

class BetaApp @Inject() (val authActions: AuthActions)
      extends AtomController {

  def index(id: String = "") = Action {

      Ok(views.html.BetaApp.app("Media Atom Maker"))
    }

}
