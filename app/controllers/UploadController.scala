package controllers

import java.util.UUID

import _root_.model.MediaAtom
import _root_.model.commands.CommandExceptions._
import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.PermissionsUploadHelper._
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoAsset, VideoSource}
import com.gu.media.upload._
import com.gu.media.upload.model._
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.User
import controllers.UploadController.CreateRequest
import data.{DataStores, UnpackedDataStores}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.{Format, Json}
import util.{AWSConfig, CredentialsGenerator, StepFunctions}

class UploadController(override val authActions: HMACAuthActions, awsConfig: AWSConfig, stepFunctions: StepFunctions,
                       override val stores: DataStores, override val permissions: MediaAtomMakerPermissionsProvider)

  extends AtomController with Logging with JsonRequestParsing with UnpackedDataStores {

  import authActions.APIAuthAction

  private val credsGenerator = new CredentialsGenerator(awsConfig)

  def list(atomId: String) = APIAuthAction {
    // Anyone can see the running uploads.
    // Only users with permission can create/complete/delete them.
    val running = stepFunctions.getStatus(atomId)
    Ok(Json.toJson(running))
  }

  def create = CanUploadAsset { implicit raw =>
    parse(raw) { req: CreateRequest =>
      if(canPerformUpload(raw.permissions, req.selfHost)) {
        log.info(s"Request for upload under atom ${req.atomId}. filename=${req.filename}. size=${req.size}, selfHosted=${req.selfHost}")

        val atom = MediaAtom.fromThrift(getPreviewAtom(req.atomId))
        val upload = buildUpload(atom, raw.user, req.size, req.selfHost, req.syncWithPluto)

        stepFunctions.start(upload)

        log.info(s"Upload created under atom ${req.atomId}. upload=${upload.id}. parts=${upload.parts.size}, selfHosted=${upload.metadata.selfHost}")
        Ok(Json.toJson(upload))
      } else {
        Unauthorized(s"User ${raw.user.email} is not authorised with permissions to upload asset, self-hosted value: ${req.selfHost}")
      }
    }
  }

  def credentials(id: String, key: String) = CanUploadAsset { implicit req =>
    getPart(id, key) match {
      case Some(part) =>
        val credentials = credsGenerator.forKey(part.key)
        Ok(Json.toJson(credentials))

      case None =>
        NotFound
    }
  }

  private def buildUpload(atom: MediaAtom, user: User, size: Long, selfHosted: Boolean, syncWithPluto: Boolean) = {
    val id = s"${atom.id}--${UUID.randomUUID().toString}"

    val plutoData = PlutoSyncMetadata(
      enabled = syncWithPluto,
      projectId = atom.plutoData.flatMap(_.projectId),
      s3Key = CompleteUploadKey(awsConfig.userUploadFolder, id).toString,
      assetVersion = -1,
      atomId = atom.id
    )

    val asset = if(!selfHosted) {
      None
    } else {
      val mp4Key = TranscoderOutputKey(id, "mp4").toString

      Some(SelfHostedAsset(List(
        VideoSource(mp4Key, "video/mp4")
      )))
    }

    val metadata = UploadMetadata(
      user = user.email,
      bucket = awsConfig.userUploadBucket,
      region = awsConfig.region.getName,
      title = atom.title,
      channel = atom.channelId.getOrElse { AtomMissingYouTubeChannel },
      pluto = plutoData,
      selfHost = selfHosted,
      asset = asset
    )

    val progress = UploadProgress(
      chunksInS3 = 0,
      chunksInYouTube = 0,
      fullyUploaded = false,
      fullyTranscoded = false,
      retries = 0
    )

    val parts = chunk(id, size)

    Upload(id, parts, metadata, progress)
  }

  private def chunk(uploadId: String, size: Long): List[UploadPart] = {
    val boundaries = Upload.calculateChunks(size)

    boundaries.zipWithIndex.map { case ((start, end), id) =>
      UploadPart(UploadPartKey(awsConfig.userUploadFolder, uploadId, id).toString, start, end)
    }
  }

  private def getPart(id: String, key: String): Option[UploadPart] = for {
    upload <- stepFunctions.getById(id)
    part <- upload.parts.find(_.key == key)
  } yield part
}

object UploadController {
  case class CreateRequest(atomId: String, filename: String, size: Long, selfHost: Boolean, syncWithPluto: Boolean)
  case class CreateResponse(id: String, region: String, bucket: String, parts: List[UploadPart])

  implicit val createRequestFormat: Format[CreateRequest] = Jsonx.formatCaseClass[CreateRequest]
  implicit val createResponseFormat: Format[CreateResponse] = Jsonx.formatCaseClass[CreateResponse]
}
