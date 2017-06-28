package controllers

import java.util.UUID

import _root_.model.{ClientAsset, MediaAtom}
import _root_.model.commands.CommandExceptions._
import com.amazonaws.services.stepfunctions.model.ExecutionListItem
import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoSource}
import com.gu.media.upload._
import com.gu.media.upload.model._
import com.gu.media.youtube.YouTubeVideos
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.User
import controllers.UploadController.CreateRequest
import data.{DataStores, UnpackedDataStores}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.{Format, Json}
import util.{AWSConfig, CredentialsGenerator, StepFunctions}

class UploadController(override val authActions: HMACAuthActions, awsConfig: AWSConfig, stepFunctions: StepFunctions,
                       override val stores: DataStores, override val permissions: MediaAtomMakerPermissionsProvider,
                       youTube: YouTubeVideos)

  extends AtomController with Logging with JsonRequestParsing with UnpackedDataStores {

  import authActions.APIAuthAction

  private val credsGenerator = new CredentialsGenerator(awsConfig)

  def list(atomId: String) = APIAuthAction {
    val atom = MediaAtom.fromThrift(getPreviewAtom(atomId))
    val added = ClientAsset.byVersion(atom.assets, youTube.getProcessingStatus)

    val jobs = stepFunctions.getJobs(atomId)
    val running = jobs.flatMap(getRunning)

    val assets = running ++ added
    Ok(Json.toJson(assets))
  }

  def create = LookupPermissions { implicit raw =>
    parse(raw) { req: CreateRequest =>
      if(req.selfHost && !raw.permissions.addSelfHostedAsset) {
        Unauthorized(s"User ${raw.user.email} is not authorised with permissions to upload self-hosted asset")
      } else {
        log.info(s"Request for upload under atom ${req.atomId}. filename=${req.filename}. size=${req.size}, selfHosted=${req.selfHost}")

        val atom = MediaAtom.fromThrift(getPreviewAtom(req.atomId))
        val upload = buildUpload(atom, raw.user, req.size, req.selfHost, req.syncWithPluto)

        stepFunctions.start(upload)

        log.info(s"Upload created under atom ${req.atomId}. upload=${upload.id}. parts=${upload.parts.size}, selfHosted=${upload.metadata.selfHost}")
        Ok(Json.toJson(upload))
      }
    }
  }

  def credentials(id: String, key: String) = LookupPermissions { implicit req =>
    getPart(id, key) match {
      case Some(part) =>
        val credentials = credsGenerator.forKey(part.key)
        Ok(Json.toJson(credentials))

      case None =>
        NotFound
    }
  }

  private def buildUpload(atom: MediaAtom, user: User, size: Long, selfHosted: Boolean, syncWithPluto: Boolean) = {
    val uploadId = UUID.randomUUID().toString
    val id = s"${atom.id}--$uploadId"

    val plutoData = PlutoSyncMetadata(
      enabled = syncWithPluto,
      projectId = atom.plutoData.flatMap(_.projectId),
      s3Key = CompleteUploadKey(awsConfig.userUploadFolder, id).toString,
      assetVersion = -1,
      atomId = atom.id
    )

    val metadata = UploadMetadata(
      user = user.email,
      bucket = awsConfig.userUploadBucket,
      region = awsConfig.region.getName,
      title = atom.title,
      pluto = plutoData,
      selfHost = selfHosted,
      runtime = getRuntimeMetadata(selfHosted, atom.channelId),
      asset = getAsset(selfHosted, atom.title, uploadId)
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

  private def getAsset(selfHosted: Boolean, title: String, uploadId: String): Option[SelfHostedAsset] = {
    if(!selfHosted) {
      // YouTube assets are added after they have been uploaded (once we know the ID)
      None
    } else {
      val mp4Key = TranscoderOutputKey(title, uploadId, "mp4").toString
      val mp4Source = VideoSource(mp4Key, "video/mp4")

      Some(SelfHostedAsset(List(mp4Source)))
    }
  }

  private def getRuntimeMetadata(selfHosted: Boolean, atomChannel: Option[String]) = atomChannel match {
    case _ if selfHosted => SelfHostedUploadMetadata(List.empty)
    case Some(channel) => YouTubeUploadMetadata(channel, uri = None)
    case None => AtomMissingYouTubeChannel
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

  private def getRunning(job: ExecutionListItem): Option[ClientAsset] = {
    val events = stepFunctions.getEventsInReverseOrder(job)

    val upload = stepFunctions.getTaskEntered(events)
    val error = stepFunctions.getExecutionFailed(events)

    upload.map { case(state, upload) =>
      ClientAsset(state, upload, error)
    }
  }
}

object UploadController {
  case class CreateRequest(atomId: String, filename: String, size: Long, selfHost: Boolean, syncWithPluto: Boolean)
  case class CreateResponse(id: String, region: String, bucket: String, parts: List[UploadPart])

  implicit val createRequestFormat: Format[CreateRequest] = Jsonx.formatCaseClass[CreateRequest]
  implicit val createResponseFormat: Format[CreateResponse] = Jsonx.formatCaseClass[CreateResponse]
}
