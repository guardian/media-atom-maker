package controllers

import com.gu.atom.data._
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.service.GoogleAuthException
import play.api.{Configuration, Logger}
import play.api.libs.ws.WSClient
import play.api.mvc._
import views.html.MediaAtom._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MainApp (previewDataStore: PreviewDataStore,
               publishedDataStore: PublishedDataStore,
               wsClient: WSClient,
               conf: Configuration,
               val authActions: HMACAuthActions)
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
    previewDataStore.listAtoms.fold(
      err => InternalServerError(err.msg),
      atoms => Ok(displayAtomList())
    )
  }
}
