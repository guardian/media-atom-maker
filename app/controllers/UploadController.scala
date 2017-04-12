package controllers

import java.util.UUID

import _root_.model.{MediaAtom, AuditEvent}
import _root_.model.commands.CommandExceptions._
import com.gu.editorial.permissions.client.PermissionsProvider
import com.gu.media.logging.Logging
import com.gu.media.upload._
import com.gu.media.upload.actions.{CopyParts, DeleteParts, UploadActionSender, UploadPartToYouTube}
import com.gu.media.upload.model._
import com.gu.media.youtube.{YouTubeAccess, YouTubeUploader}
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.action.UserRequest
import com.gu.pandomainauth.model.User
import controllers.UploadController.{CompleteResponse, CreateRequest}
import data.{DataStores, UnpackedDataStores}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.{Format, Json}
import play.api.mvc.Result
import util.AWSConfig
import com.gu.pandomainauth.model.{User => PandaUser}

class UploadController(override val authActions: HMACAuthActions, awsConfig: AWSConfig, youTube: YouTubeAccess,
                       uploadActions: UploadActionSender, override val stores: DataStores,
                       override val permissions: PermissionsProvider)

  extends AtomController with Logging with JsonRequestParsing with UnpackedDataStores {

  import authActions.APIHMACAuthAction

  private val UPLOAD_KEY_HEADER = "X-Upload-Key"
  private val UPLOAD_URI_HEADER = "X-Upload-Uri"

  private val table = stores.uploadStore
  private val credsGenerator = new CredentialsGenerator(awsConfig)
  private val uploader = new YouTubeUploader(awsConfig, youTube)

  def list(atomId: String) = APIHMACAuthAction {
    // Anyone can see the running uploads.
    // Only users with permission can create/complete/delete them.
    val uploads = table.list(atomId)
    Ok(Json.toJson(uploads))
  }

  def create = CanAddAsset { implicit raw =>
    parse(raw) { req: CreateRequest =>
      log.info(s"Request for upload under atom ${req.atomId}. filename=${req.filename}. size=${req.size}")

      val atom = MediaAtom.fromThrift(getPreviewAtom(req.atomId))
      val upload = buildUpload(atom, raw.user, req.size)
      table.put(upload)

      val event = AuditEvent.startUpload(raw.user, req.atomId, upload.id, req.filename, req.size)
      stores.audit.putAuditEvent(event)

      Ok(Json.toJson(upload))
    }
  }

  def delete(id: String) = CanAddAsset { implicit req =>
    table.delete(id)
    NoContent
  }

  def credentials(id: String) = CanAddAsset { implicit req =>
    partRequest(id, req) { (upload, part, _) =>
      val credentials = credsGenerator.forKey(upload.id, part.key)
      Ok(Json.toJson(credentials))
    }
  }

  def complete(id: String) = CanAddAsset { implicit req =>
    partRequest(id, req) {
      case (upload, part, Some(uploadUri)) =>
        partComplete(upload, part, uploadUri, req.user)
        Ok(Json.toJson(CompleteResponse(uploadUri)))


      case (upload, part, None) =>
        val uploadUri = uploader.startUpload(upload.metadata.title, upload.metadata.channel, upload.id, upload.parts.last.end)
        partComplete(upload, part, uploadUri, req.user)
        Ok(Json.toJson(CompleteResponse(uploadUri)))
    }
  }

  private def buildUpload(atom: MediaAtom, user: User, size: Long) = {
    val id = UUID.randomUUID().toString

    val plutoData = PlutoSyncMetadata(
      projectId = atom.plutoProjectId,
      s3Key = CompleteUploadKey(awsConfig.userUploadFolder, id).toString,
      assetVersion = -1,
      atomId = atom.id
    )

    val metadata = UploadMetadata(
      user = user.email,
      bucket = awsConfig.userUploadBucket,
      region = awsConfig.region.getName,
      title = atom.title,
      channel = atom.channelId.getOrElse { AtomMissingYouTubeChannel },
      pluto = plutoData
    )

    val parts = chunk(id, size)

    Upload(id, parts, metadata, UploadProgress(0, 0))
  }

  private def partRequest(id: String, request: UserRequest[_])(fn: (Upload, UploadPart, Option[String]) => Result): Result = {
    table.get(id) match {
      case Some(upload) =>
        request.headers.get(UPLOAD_KEY_HEADER) match {
          case Some(key) =>
            upload.parts.find(_.key == key) match {
              case Some(part) =>
                fn(upload, part, request.headers.get(UPLOAD_URI_HEADER))

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

  private def partComplete(upload: Upload, part: UploadPart, uploadUri: String, user: PandaUser): Upload = {
    val complete = upload.copy(progress = upload.progress.copy(uploadedToS3 = part.end))

    table.report(complete)
    uploadActions.send(UploadPartToYouTube(upload, part, uploadUri))

    if(part.key == upload.parts.last.key) {
      val completeKey = CompleteUploadKey(awsConfig.userUploadFolder, complete.id).toString

      uploadActions.send(CopyParts(upload, completeKey))
      uploadActions.send(DeleteParts(upload))

      val event = AuditEvent.s3UploadComplete(user, upload.metadata.pluto.atomId, upload.id)
      stores.audit.putAuditEvent(event)
    }

    complete
  }

  private def chunk(uploadId: String, size: Long): List[UploadPart] = {
    val boundaries = Upload.calculateChunks(size)

    boundaries.zipWithIndex.map { case ((start, end), id) =>
      UploadPart(UploadPartKey(awsConfig.userUploadFolder, uploadId, id).toString, start, end)
    }
  }
}

object UploadController {
  case class CreateRequest(atomId: String, filename: String, size: Long)
  case class CreateResponse(id: String, region: String, bucket: String, parts: List[UploadPart])
  case class CompleteResponse(uploadUri: String)

  implicit val createRequestFormat: Format[CreateRequest] = Jsonx.formatCaseClass[CreateRequest]
  implicit val createResponseFormat: Format[CreateResponse] = Jsonx.formatCaseClass[CreateResponse]
  implicit val completeResponseFormat: Format[CompleteResponse] = Jsonx.formatCaseClass[CompleteResponse]
}
