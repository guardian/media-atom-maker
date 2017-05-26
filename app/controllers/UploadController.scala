package controllers

import java.util.UUID

import _root_.model.MediaAtom
import _root_.model.commands.CommandExceptions._
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest
import com.gu.media.logging.Logging
import com.gu.media.upload._
import com.gu.media.upload.actions.{CopyParts, DeleteParts, UploadActionSender, UploadPartToYouTube, UploadPartsToSelfHost}
import com.gu.media.upload.model._
import com.gu.media.youtube.{YouTubeAccess, YouTubeUploader}
import com.gu.media.{MediaAtomMakerPermissionsProvider, Permissions, PermissionsUploadHelper}
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.User
import controllers.UploadController.{CompleteResponse, CreateRequest}
import data.{DataStores, UnpackedDataStores}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Request, Result}
import util.AWSConfig

class UploadController(override val authActions: HMACAuthActions, awsConfig: AWSConfig, youTube: YouTubeAccess,
                       uploadActions: UploadActionSender, override val stores: DataStores,
                       override val permissions: MediaAtomMakerPermissionsProvider)

  extends AtomController with Logging with JsonRequestParsing with UnpackedDataStores {

  import authActions.APIAuthAction

  private val UPLOAD_KEY_HEADER = "X-Upload-Key"
  private val UPLOAD_URI_HEADER = "X-Upload-Uri"
  private val UPLOAD_METHOD_HEADER = "X-Upload-Method"

  private val table = stores.uploadStore
  private val credsGenerator = new CredentialsGenerator(awsConfig)
  private val uploader = YouTubeUploader(awsConfig, youTube)

  def list(atomId: String) = APIAuthAction {
    // Anyone can see the running uploads.
    // Only users with permission can create/complete/delete them.
    val uploads = table.list(atomId)
    Ok(Json.toJson(uploads))
  }

  def create = CanUploadAsset { implicit raw =>
    parse(raw) { req: CreateRequest =>
      val useStepFunctions = raw.headers.get(UPLOAD_METHOD_HEADER).contains("StepFunctions")

      if (PermissionsUploadHelper.canPerformUpload(raw.permissions, req.selfHost)) {
        log.info(s"Request for upload under atom ${req.atomId}. filename=${req.filename}. size=${req.size}, selfHosted=${req.selfHost}")
        val atom = MediaAtom.fromThrift(getPreviewAtom(req.atomId))
        val upload = buildUpload(atom, raw.user, req.size, req.selfHost, req.syncWithPluto, useStepFunctions)
        table.put(upload)

        if(useStepFunctions) {
          val stepFunctionsRequest = new StartExecutionRequest()
            .withName(s"${upload.metadata.pluto.atomId}--${upload.id}")
            .withStateMachineArn(awsConfig.pipelineArn)
            .withInput(Json.stringify(Json.toJson(upload)))

          awsConfig.stepFunctionsClient.startExecution(stepFunctionsRequest)
        }

        log.info(s"Upload created under atom ${req.atomId}. upload=${upload.id}. parts=${upload.parts.size}, selfHosted=${upload.metadata.selfHost}")
        Ok(Json.toJson(upload))
      }
      else Unauthorized(s"User ${raw.user.email} is not authorised with permissions to upload asset, self-hosted value: ${req.selfHost}")
    }
  }

  def delete(id: String) = CanUploadAsset { implicit req =>
    table.delete(id)
    NoContent
  }

  def credentials(id: String) = CanUploadAsset { implicit req =>
    partRequest(id, req) { (upload, part, _) =>
      val credentials = credsGenerator.forKey(upload.id, part.key)
      Ok(Json.toJson(credentials))
    }
  }

  def complete(id: String) = CanUploadAsset { implicit req =>
    partRequest(id, req) { (upload, part, optionalUri) =>
      val selfHost = upload.metadata.selfHost
      if(!PermissionsUploadHelper.canPerformUpload(req.permissions, selfHost))
        Unauthorized(s"User ${req.user.email} is not authorised with permissions to complete upload, self-hosted value: ${selfHost}")
      else if (upload.metadata.useStepFunctions)
        Ok(Json.toJson(CompleteResponse(s"${upload.id} running using step functions")))
      else if (selfHost)
        startUploadToSelfHost(upload, part, req.permissions)
      else
        startUploadToYouTube(upload, part, optionalUri, req.permissions)
    }
  }

  private def startUploadToSelfHost(upload: Upload, part: UploadPart, permission: Permissions) = {
    if(part == upload.parts.last) {
      completeAndDeleteParts(upload, part)
      val key = completeKey(upload)
      uploadActions.send(UploadPartsToSelfHost(upload, key, awsConfig.transcodePipelineId))
      log.info(s"$key has been sent to transcoding to pipeline ${awsConfig.transcodePipelineId}")
      Ok(Json.toJson(CompleteResponse(s"$key has been sent for transcoding to S3")))
    }
    else {
      log.info(s"${upload.id} has not finished uploading to S3 yet")
      Ok(Json.toJson(CompleteResponse(s"${upload.id} has not finished uploading to S3 yet")))
    }
  }

  private def startUploadToYouTube(upload: Upload, part: UploadPart, uploadUri: Option[String], permission: Permissions) = {
    uploadUri.map { u =>
      partComplete(upload, part, u)
      Ok(Json.toJson(CompleteResponse(u)))
    }.getOrElse {
      val uploadUri = uploader.startUpload(upload.metadata.title, upload.metadata.channel, upload.id, upload.parts.last.end)
      log.info(s"Starting upload to YouTube. atom=${upload.metadata.pluto.atomId} upload=${upload.id} uploadUri=${uploadUri}")

      partComplete(upload, part, uploadUri)
      Ok(Json.toJson(CompleteResponse(uploadUri)))
    }
  }

  private def buildUpload(atom: MediaAtom, user: User, size: Long, selfHosted: Boolean, syncWithPluto: Boolean, useStepFunctions: Boolean) = {
    val id = UUID.randomUUID().toString

    val plutoData = PlutoSyncMetadata(
      enabled = syncWithPluto,
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
      pluto = plutoData,
      selfHost = selfHosted,
      useStepFunctions = useStepFunctions
    )

    val progress = UploadProgress(
      uploadedToS3 = 0,
      uploadedToYouTube = 0,
      chunksInS3 = 0,
      fullyUploaded = false,
      fullyTranscoded = false,
      retries = 0
    )

    val parts = chunk(id, size)

    Upload(id, parts, metadata, progress)
  }

  private def partRequest(id: String, request: Request[_])(fn: (Upload, UploadPart, Option[String]) => Result): Result = {
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

  private def partComplete(upload: Upload, part: UploadPart, uploadUri: String): Upload = {
    val complete = upload.copy(progress = upload.progress.copy(uploadedToS3 = part.end))
    table.report(complete)
    uploadActions.send(UploadPartToYouTube(upload, part, uploadUri))

    log.info(s"Part ${part.key} uploaded to S3. upload=${upload.id} atom=${upload.metadata.pluto.atomId}")

    completeAndDeleteParts(upload, part)
    complete
  }

  private def completeAndDeleteParts(upload: Upload, part: UploadPart) {
    if(part.key == upload.parts.last.key) {
      log.info(s"All parts uploaded to S3. upload=${upload.id} atom=${upload.metadata.pluto.atomId}")

      uploadActions.send(CopyParts(upload, completeKey(upload)))
      uploadActions.send(DeleteParts(upload))
    }
  }

  private def completeKey(upload: Upload) = CompleteUploadKey(awsConfig.userUploadFolder, upload.id).toString

  private def chunk(uploadId: String, size: Long): List[UploadPart] = {
    val boundaries = Upload.calculateChunks(size)

    boundaries.zipWithIndex.map { case ((start, end), id) =>
      UploadPart(UploadPartKey(awsConfig.userUploadFolder, uploadId, id).toString, start, end)
    }
  }
}

object UploadController {
  case class CreateRequest(atomId: String, filename: String, size: Long, selfHost: Boolean, syncWithPluto: Boolean)
  case class CreateResponse(id: String, region: String, bucket: String, parts: List[UploadPart])
  case class CompleteResponse(uploadUri: String)

  implicit val createRequestFormat: Format[CreateRequest] = Jsonx.formatCaseClass[CreateRequest]
  implicit val createResponseFormat: Format[CreateResponse] = Jsonx.formatCaseClass[CreateResponse]
  implicit val completeResponseFormat: Format[CompleteResponse] = Jsonx.formatCaseClass[CompleteResponse]
}
