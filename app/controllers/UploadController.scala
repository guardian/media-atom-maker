package controllers

import com.gu.media.logging.Logging
import com.gu.media.upload._
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.action.UserRequest
import com.gu.pandomainauth.model.User
import controllers.UploadController.CreateRequest
import data.{DataStores, UnpackedDataStores}
import model.MediaAtom
import org.cvogt.play.json.Jsonx
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Controller, Result}
import util.AWSConfig
import model.commands.CommandExceptions.AtomMissingYouTubeChannel

class UploadController(val authActions: HMACAuthActions, awsConfig: AWSConfig, override val stores: DataStores)
  extends Controller with Logging with JsonRequestParsing with UnpackedDataStores {

  import authActions.APIHMACAuthAction

  private val UPLOAD_KEY_HEADER = "X-Upload-Key"
  private val table = new UploadsDataStore(awsConfig)
  private val uploads = new UploadLifecycle(awsConfig)

  def list(atomId: String) = APIHMACAuthAction {
    val uploads = table.list(atomId)
    Ok(Json.toJson(uploads))
  }

  def create = APIHMACAuthAction { implicit raw =>
    parse(raw) { req: CreateRequest =>
      log.info(s"Request for upload under atom ${req.atomId}. filename=${req.filename}. size=${req.size}")

      val atom = MediaAtom.fromThrift(getPreviewAtom(req.atomId))
      val upload = buildUpload(atom, raw.user, req.size)
      table.put(upload)

      Ok(Json.toJson(upload))
    }
  }

  def delete(id: String) = APIHMACAuthAction {
    table.delete(id)
    NoContent
  }

  def credentials(id: String) = APIHMACAuthAction { implicit req =>
    partRequest(id, req) { (upload, part) =>
      val credentials = uploads.credentialsForPart(upload, part)
      Ok(Json.toJson(credentials))
    }
  }

  def complete(id: String) = APIHMACAuthAction { implicit req =>
    partRequest(id, req) { (upload, part) =>
      val complete = uploads.partComplete(upload, part)
      table.put(complete)

      NoContent
    }
  }

  private def buildUpload(atom: MediaAtom, user: User, size: Long) = {
    val metadata = UploadMetadata(
      atomId = atom.id,
      user = user.email,
      bucket = awsConfig.userUploadBucket,
      region = awsConfig.region.getName,
      title = atom.title,
      plutoProjectId = atom.plutoProjectId
    )

    val youTube = YouTubeMetadata(
      channel = atom.channelId.getOrElse { AtomMissingYouTubeChannel },
      upload = None
    )

    uploads.create(metadata, youTube, size)
  }

  private def partRequest(id: String, request: UserRequest[_])(fn: (Upload, UploadPart) => Result): Result = {
    table.get(id) match {
      case Some(upload) =>
        request.headers.get(UPLOAD_KEY_HEADER) match {
          case Some(key) =>
            upload.parts.find(_.key == key) match {
              case Some(part) =>
                fn(upload, part)

              case None =>
                BadRequest(s"Unknown part key $key")
            }

          case None =>
            BadRequest(s"Missing header $UPLOAD_KEY_HEADER")
        }

      case None =>
        BadRequest(s"Unknown upload id $id")
    }
  }
}

object UploadController {
  case class CreateRequest(atomId: String, filename: String, size: Long)
  case class CreateResponse(id: String, region: String, bucket: String, parts: List[UploadPart])

  implicit val createRequestFormat: Format[CreateRequest] = Jsonx.formatCaseClass[CreateRequest]
  implicit val createResponseFormat: Format[CreateResponse] = Jsonx.formatCaseClass[CreateResponse]
}
