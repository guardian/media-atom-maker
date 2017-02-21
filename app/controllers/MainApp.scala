package controllers

import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.service.GoogleAuthException
import data.DataStores
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.html.MediaAtom._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MainApp (override val stores: DataStores, conf: Configuration, val authActions: HMACAuthActions)
  extends AtomController {

  import authActions.{AuthAction, processGoogleCallback}

  def healthcheck = Action {
    Ok(s"ok\ngitCommitID ${app.BuildInfo.gitCommitId}")
  }

  def oauthCallback = Action.async { implicit req =>
    try {
      processGoogleCallback()
    } catch {
      case e: GoogleAuthException => {
        val redirectTo = "https://" + conf.getString("host").get
        Logger.info(s"Authentication failure. ${e.message}. Redirecting to $redirectTo")
        Future(Redirect(redirectTo, MOVED_PERMANENTLY))
      }
    }
  }

  def listAtoms = AuthAction { implicit req =>
    stores.preview.listAtoms.fold(
      err => InternalServerError(err.msg),
      atoms => Ok(displayAtomList())
    )
  }
}
