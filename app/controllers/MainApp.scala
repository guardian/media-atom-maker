package controllers

import com.gu.contentatom.thrift.{ Atom, AtomData }
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.action.AuthActions
import javax.inject._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import model.ThriftUtil
import ThriftUtil.ThriftResult
import views.html.MediaAtom._
import data._
import com.gu.atom.data._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.Logger


class MainApp @Inject() (previewDataStore: PreviewDataStore,
                         publishedDataStore: PublishedDataStore,
                         val wsClient: WSClient,
                         val conf: Configuration,
                         val authActions: HMACAuthActions)
    extends AtomController {

  import authActions.{ AuthAction, processGoogleCallback }

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
