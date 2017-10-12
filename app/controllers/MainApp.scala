package controllers

import com.gu.editorial.permissions.client.PermissionsProvider
import com.gu.media.aws.KinesisAccess
import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.pandahmac.HMACAuthActions
import data.DataStores
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc._
import views.html.MediaAtom._

class MainApp (override val stores: DataStores,
               wsClient: WSClient,
               conf: Configuration,
               val authActions: HMACAuthActions,
               val permissions: MediaAtomMakerPermissionsProvider,
               kinesis: KinesisAccess)
    extends AtomController {

  import authActions.AuthAction

  def listAtoms = AuthAction { implicit req =>
    val gaPropertyId = conf.getString("gaPropertyId")

    previewDataStore.listAtoms.fold(
      err => InternalServerError(err.msg),
      atoms => Ok(displayAtomList(gaPropertyId))
    )
  }
}
