package controllers

import com.gu.contentatom.thrift.{ Atom, AtomData }
import com.gu.pandomainauth.action.AuthActions
import javax.inject._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import model.ThriftUtil
import ThriftUtil.ThriftResult
import views.html.MediaAtom._
import data._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.Logger

class MainApp @Inject() (dataStore: DataStore,
                         val wsClient: WSClient,
                         val conf: Configuration,
                         val authActions: AuthActions)
    extends AtomController {

  import authActions.{ AuthAction, processGoogleCallback }

  def healthcheck = Action {
    Ok("ok")
  }

  def oauthCallback = Action.async { implicit req =>
    processGoogleCallback()
  }

  def getAtom(id: String) = AuthAction { implicit req =>
    dataStore.getMediaAtom(id) match {
      case Some(atom) => Ok(displayAtom(atom))
      case None => NotFound(s"no atom with id $id found")
    }
  }

  def listAtoms = AuthAction { implicit req =>
    dataStore.listAtoms.fold(
      err => InternalServerError(err.msg),
      atoms => Ok(displayAtomList(atoms.toList))
    )
  }
}
