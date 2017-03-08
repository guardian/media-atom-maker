package controllers

import java.util.UUID

import com.gu.media.logging.Logging
import com.gu.media.upload._
import com.gu.pandahmac.HMACAuthActions
import controllers.UploadController.CreateRequest
import data.{DataStores, UnpackedDataStores}
import model.MediaAtom
import org.cvogt.play.json.Jsonx
import play.api.libs.json.{Format, Json}
import play.api.mvc.Controller
import util.AWSConfig

class UploadController(val authActions: HMACAuthActions, awsConfig: AWSConfig, override val stores: DataStores)
  extends Controller with Logging with JsonRequestParsing with UnpackedDataStores {

  import authActions.APIHMACAuthAction

  private val table = new DynamoUploadsTable(awsConfig)
  private val creds = new CredentialsGenerator(awsConfig)

  def list(atomId: String) = APIHMACAuthAction {
    val results = table.list(atomId)
    Ok(Json.toJson(results))
  }

  def create = APIHMACAuthAction { implicit raw =>
    parse(raw) { req: CreateRequest =>
      log.info(s"Request for upload under atom ${req.atomId}. filename=${req.filename}. size=${req.size}")

      val atom = MediaAtom.fromThrift(getPreviewAtom(req.atomId))
      val id = UUID.randomUUID().toString

      val parts = UploadPart.build(req.size)
      val upload = Upload(id, atom.id, parts)

      table.put(upload)
      Ok(Json.toJson(upload))
    }
  }

  def stop(id: String) = APIHMACAuthAction {
    table.delete(id)
    NoContent
  }

  def credentials(id: String, part: String) = APIHMACAuthAction {
    try {
      val key = UploadPartKey(awsConfig.userUploadFolder, id, part.toInt)
      val credentials = creds.forPart(key)

      Ok(Json.toJson(credentials))
    } catch {
      case err: NumberFormatException =>
        BadRequest(err.getMessage)
    }
  }
}

object UploadController {
  case class CreateRequest(atomId: String, filename: String, size: Long)
  case class CreateResponse(id: String, region: String, bucket: String, parts: List[UploadPart])

  implicit val createRequestFormat: Format[CreateRequest] = Jsonx.formatCaseClass[CreateRequest]
  implicit val createResponseFormat: Format[CreateResponse] = Jsonx.formatCaseClass[CreateResponse]
}
