package controllers

import java.util.UUID

import com.gu.media.logging.Logging
import com.gu.media.upload._
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.User
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

  private val UPLOAD_KEY_HEADER = "X-Upload-Key"
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
      atom.channelId match {
        case Some(channel) =>
          val upload = buildUpload(atom, channel, raw.user, req.size)

          table.put(upload)
          Ok(Json.toJson(upload))

        case None =>
          BadRequest("Atom missing YouTube channel")
      }
    }
  }

  def delete(id: String) = APIHMACAuthAction {
    table.delete(id)
    NoContent
  }

  def credentials(id: String) = APIHMACAuthAction { implicit req =>
    (table.consistentlyGet(id), req.headers.get(UPLOAD_KEY_HEADER)) match {
      case (Some(upload), Some(key)) =>
        val validKey = upload.parts.exists(_.key == key)

        if(validKey) {
          val credentials = creds.forKey(id, key)
          Ok(Json.toJson(credentials))
        } else {
          BadRequest(s"Unknown part key $key")
        }

      case _ =>
        BadRequest(s"Unknown upload or missing $UPLOAD_KEY_HEADER. id=$id")
    }
  }

  private def chunk(uploadId: String, size: Long): List[UploadPart] = {
    val boundaries = Upload.calculateChunks(size)

    boundaries.zipWithIndex.map { case ((start, end), id) =>
      UploadPart(UploadPartKey(awsConfig.userUploadFolder, uploadId, id).toString, start, end)
    }
  }

  private def buildUpload(atom: MediaAtom, channelId: String, user: User, size: Long) = {
    val id = UUID.randomUUID().toString

    val parts = chunk(id, size)

    val metadata = UploadMetadata(
      atomId = atom.id,
      user = user.email,
      bucket = awsConfig.userUploadBucket,
      region = awsConfig.region.getName
    )

    val youTube = YouTubeMetadata(
      title = atom.title,
      channel = channelId,
      upload = None
    )

    Upload(id, parts, metadata, youTube)
  }
}

object UploadController {
  case class CreateRequest(atomId: String, filename: String, size: Long)
  case class CreateResponse(id: String, region: String, bucket: String, parts: List[UploadPart])

  implicit val createRequestFormat: Format[CreateRequest] = Jsonx.formatCaseClass[CreateRequest]
  implicit val createResponseFormat: Format[CreateResponse] = Jsonx.formatCaseClass[CreateResponse]
}
