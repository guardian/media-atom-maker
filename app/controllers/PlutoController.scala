package controllers

import java.util.Locale
import com.gu.media.aws._
import com.gu.media.Settings
import com.gu.media.logging.Logging
import com.gu.media.pluto.PlutoUpsertRequest
import com.gu.media.upload.PlutoUploadActions
import com.gu.pandahmac.HMACAuthActions
import data.{DataStores, UnpackedDataStores}
import play.api.libs.json.Json
import play.api.mvc.Controller
import com.gu.media.model.{MediaAtom, MediaAtomBeforeCreation, PlutoSyncMetadataMessage}
import com.typesafe.config.{Config, ConfigFactory}

class PlutoController(
  val config:Config,
  val credentials:AwsCredentials,
  val authActions: HMACAuthActions,
  override val stores: DataStores
) extends Controller
  with UnpackedDataStores
  with JsonRequestParsing
  with Settings
  with AwsAccess
  with DynamoAccess
  with KinesisAccess
  with SESSettings
with UploadAccess
  with Logging {

  import authActions.{APIAuthAction, APIHMACAuthAction}

  final override def regionName = sys.env.get("REGION")
  final override def readTag(tag: String) = sys.env.get(tag.toUpperCase(Locale.ENGLISH))

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
    val pluto = new PlutoUploadActions(this)
    try {
      val atomContent = getPreviewAtom(id)
      val atom = MediaAtom.fromThrift(atomContent)
      atom.plutoData match {
        case Some(data)=>
          pluto.sendToPluto(PlutoSyncMetadataMessage.build(
            id,
            atom,
            this,
            "system@video.gutools.co.uk"
          ))
          Ok(Json.toJson(atom))
        case None=>
          BadRequest(s"$id does not have pluto data attached")
      }
    } catch {
      case excep:Throwable=>InternalServerError(excep.toString)
    }
  }
}
