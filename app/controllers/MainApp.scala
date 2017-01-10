package controllers

import javax.inject._

import com.gu.atom.data._
import com.gu.pandahmac.HMACAuthActions
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc._
import views.html.MediaAtom._


class MainApp @Inject() (previewDataStore: PreviewDataStore,
                         publishedDataStore: PublishedDataStore,
                         val wsClient: WSClient,
                         val conf: Configuration,
                         val authActions: HMACAuthActions)
    extends AtomController {

  import authActions.{AuthAction, processGoogleCallback}

  def healthcheck = Action {
    Ok("ok")
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
