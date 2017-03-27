package controllers

import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.service.GoogleAuthException
import data.DataStores
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}
import views.html.MediaAtom._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MainApp (override val stores: DataStores,
               wsClient: WSClient,
               conf: Configuration,
               val authActions: HMACAuthActions)
    extends AtomController {

  import authActions.{AuthAction, processGoogleCallback}

  def healthcheck = Action {
    Ok(s"ok\ngitCommitID ${app.BuildInfo.gitCommitId}")
  }

  def oauthCallback = Action.async { implicit req =>
    processGoogleCallback()
  }

  def listAtoms = AuthAction { implicit req =>
    previewDataStore.listAtoms.fold(
      err => InternalServerError(err.msg),
      atoms => Ok(displayAtomList())
    )
  }
}
