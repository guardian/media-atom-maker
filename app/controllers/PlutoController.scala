package controllers

import com.gu.media.aws._
import com.gu.media.Settings
import com.gu.media.logging.Logging
import com.gu.media.pluto.{PlutoCommissionDataStoreException, PlutoUpsertRequest}
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

  def getCommission(commissionId: String) = APIAuthAction {
    stores.plutoCommissionStore.getById(commissionId) match {
      case Some(Right(commission)) => Ok(Json.toJson(commission))
      case Some(Left(error)) =>
        log.error(s"failed to get pluto commission with ID $commissionId", error)
        ServiceUnavailable
      case None =>
        log.warn(s"no commission with ID $commissionId")
        NotFound
    }
  }

  def getProjectsByCommissionId(id: String) = APIAuthAction {
    val plutoProjects = stores.plutoProjectStore.getByCommissionId(id)
    Ok(Json.toJson(plutoProjects))
  }

  def getProject(projectId:String) = APIAuthAction {
    stores.plutoProjectStore.getById(projectId) match {
      case Some(Right(project)) => Ok(Json.toJson(project))
      case Some(Left(error)) =>
        log.error(s"failed to get pluto project with ID $projectId", error)
        ServiceUnavailable
      case None =>
        log.warn(s"no project with ID $projectId")
        NotFound
    }
  }

  def deleteCommission(id: String) = APIHMACAuthAction {
    stores.plutoProjectStore.deleteByCommissionId(id)
    stores.plutoCommissionStore.delete(id)
    Accepted
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

      if(MediaAtomHelpers.getCurrentAssetVersion(atom).isEmpty){
        log.warn("Requested re-index on an atom with no currentAssetVersion, returning 404")
        NotFound(Json.toJson(Map("status"->"notfound", "detail"->"atom had no current version, resync was probably sent too early")))
      }

      val versionWithId = id + s"-${MediaAtomHelpers.getCurrentAssetVersion(atom).get}"

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
