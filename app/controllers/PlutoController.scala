package controllers

import java.util.Locale
import com.gu.media.aws._
import com.gu.media.Settings
import com.gu.media.logging.Logging
import com.gu.media.pluto.PlutoUpsertRequest
import com.gu.media.upload.PlutoUploadActions
import com.gu.pandahmac.HMACAuthActions
import com.gu.media.util.MediaAtomHelpers
import data.{DataStores, UnpackedDataStores}
import play.api.libs.json.Json
import play.api.mvc.Controller
import com.gu.media.model.{MediaAtom, MediaAtomBeforeCreation, PlutoSyncMetadataMessage}
import com.typesafe.config.{Config, ConfigFactory}
import util.AWSConfig

class PlutoController(
  val config:Config,
  val awsConfig:AWSConfig,
  val authActions: HMACAuthActions,
  override val stores: DataStores
) extends Controller
  with UnpackedDataStores
  with JsonRequestParsing
  with Logging {

  import authActions.{APIAuthAction, APIHMACAuthAction}

  def getCommissions() = APIAuthAction {
    val plutoCommissions = stores.plutoCommissionStore.list()
    Ok(Json.toJson(plutoCommissions))
  }

  def getProjectsByCommissionId(id: String) = APIAuthAction {
    val plutoProjects = stores.plutoProjectStore.getByCommissionId(id)
    Ok(Json.toJson(plutoProjects))
  }

  def upsertProject() = APIHMACAuthAction { implicit req =>
    parse[PlutoUpsertRequest](req) { data: PlutoUpsertRequest => {
      val project = stores.plutoProjectStore.upsert(data)
      Ok(Json.toJson(project))
    }}
  }

  def resendAtomMessage(id: String) = APIHMACAuthAction {
    val settings = com.gu.media.Settings(this.config)
    val pluto = new PlutoUploadActions(awsConfig)
    try {
      val atomContent = getPreviewAtom(id)
      val atom = MediaAtom.fromThrift(atomContent)
      val versionWithId = MediaAtomHelpers.getCurrentAssetVersion(atom) match {
        case Some(versionNumber)=>id + s"-$versionNumber"
        case None=>
          log.error("Requested re-index on an atom with no currentAssetVersion, this could indicate a problem.")
          id
      }

      atom.plutoData match {
        case Some(data)=>
          pluto.sendToPluto(PlutoSyncMetadataMessage.build(
            versionWithId,
            atom,
            awsConfig,
            "system@video.gutools.co.uk"
          ),shouldSendEmail=false)
          Ok(Json.toJson(atom))
        case None=>
          BadRequest(s"$id does not have pluto data attached")
      }
    } catch {
      case excep:Throwable=>InternalServerError(excep.toString)
    }
  }
}
