package controllers

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
import com.gu.media.model.{MediaAtom, MediaAtomBeforeCreation, PlutoResyncMetadataMessage}
import com.typesafe.config.{Config, ConfigFactory}
import util.AWSConfig
import model.commands.CommandExceptions._

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
          val pluto = new PlutoUploadActions(awsConfig)
          pluto.sendToPluto(PlutoResyncMetadataMessage.build(
            versionWithId,
            atom,
            awsConfig
          ))
          Ok(Json.toJson(atom))
        case None=>
          BadRequest(s"$id does not have pluto data attached")
      }
    } catch {
      commandExceptionAsResult
    }
  }
}
