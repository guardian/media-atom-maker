package controllers

import com.gu.editorial.permissions.client.PermissionsProvider
import com.gu.media.aws.KinesisAccess
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
               val authActions: HMACAuthActions,
               val permissions: PermissionsProvider,
               kinesis: KinesisAccess)
    extends AtomController {

  import authActions.{AuthAction, processGoogleCallback}

  def healthcheck = Action {
    if(kinesis.testKinesisAccess(kinesis.previewKinesisStreamName) && kinesis.testKinesisAccess(kinesis.liveKinesisStreamName)) {
      Ok(s"ok\ngitCommitID ${app.BuildInfo.gitCommitId}")
    } else {
      InternalServerError("fail. cannot access CAPI kinesis streams")
    }
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
