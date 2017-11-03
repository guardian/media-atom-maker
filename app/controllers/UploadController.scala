package controllers

import com.amazonaws.services.stepfunctions.model.{ExecutionAlreadyExistsException, ExecutionListItem}
import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.logging.Logging
import com.gu.media.model.{ClientAsset, ClientAssetProcessing, MediaAtom, YouTubeAsset}
import com.gu.media.upload.model._
import com.gu.media.util.{MediaAtomHelpers, MediaAtomImplicits}
import com.gu.media.youtube.YouTubeVideos
import com.gu.pandahmac.HMACAuthActions
import data.{DataStores, UnpackedDataStores}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.{Format, Json}
import util._

import scala.annotation.tailrec

class UploadController(override val authActions: HMACAuthActions, awsConfig: AWSConfig, stepFunctions: StepFunctions,
                       override val stores: DataStores, override val permissions: MediaAtomMakerPermissionsProvider,
                       youTube: YouTubeVideos)

  extends AtomController with Logging with JsonRequestParsing with UnpackedDataStores with MediaAtomImplicits {

  import authActions.APIAuthAction

  private val credsGenerator = new CredentialsGenerator(awsConfig)
  private val uploadDecorator = new UploadDecorator(awsConfig, stepFunctions)

  def list(atomId: String) = APIAuthAction { req =>
    val atom = MediaAtom.fromThrift(getPreviewAtom(atomId))
    val withStatus = ClientAsset.fromAssets(atom.assets).map(addYouTubeStatus)
    val assets = withStatus.map { asset => uploadDecorator.addMetadata(atom.id, asset) }

    val jobs = stepFunctions.getJobs(atomId)
    val uploads = jobs.flatMap(getRunning(assets, _))

    Ok(Json.toJson(uploads ++ assets))
  }

  def create = LookupPermissions { implicit raw =>
    parse(raw) { req: UploadRequest =>
      if(req.selfHost && !raw.permissions.addSelfHostedAsset) {
        Unauthorized(s"User ${raw.user.email} is not authorised with permissions to upload self-hosted asset")
      } else {
        log.info(s"Request for upload under atom ${req.atomId}. filename=${req.filename}. size=${req.size}, selfHosted=${req.selfHost}")

        val thriftAtom = getPreviewAtom(req.atomId)
        val atom = MediaAtom.fromThrift(thriftAtom)
        val version = MediaAtomHelpers.getNextAssetVersion(thriftAtom.tdata)

        val upload = start(atom, raw.user.email, req, version)

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

  @tailrec
  private def start(atom: MediaAtom, email: String, req: UploadRequest, version: Long): Upload = try {
    val upload = UploadBuilder.build(atom, email, version, req, awsConfig)
    stepFunctions.start(upload)

    upload
  } catch {
    case _: ExecutionAlreadyExistsException =>
      start(atom, email, req, version + 1)
  }

  private def addYouTubeStatus(video: ClientAsset): ClientAsset = video.asset match {
    case Some(YouTubeAsset(id)) =>
      val status = youTube.getProcessingStatus(id).map(ClientAssetProcessing(_))
      video.copy(processing = status)

    case _ =>
      video
  }

  private def getPart(id: String, key: String): Option[UploadPart] = for {
    upload <- stepFunctions.getById(id)
    part <- upload.parts.find(_.key == key)
  } yield part

  private def getRunning(assets: List[ClientAsset], job: ExecutionListItem): Option[ClientAsset] = {
    val alreadyAdded = assets.exists { asset =>
      job.getName.endsWith(s"-${asset.id}")
    }

    if(alreadyAdded) {
      None
    } else {
      val events = stepFunctions.getEventsInReverseOrder(job)
      val startTimestamp = job.getStartDate.getTime

      val upload = stepFunctions.getTaskEntered(events)
      val error = stepFunctions.getExecutionFailed(events)

      upload.map { case (state, upload) =>
        ClientAsset.fromUpload(state, startTimestamp, upload, error)
      }
    }
  }
}

object UploadController {
  case class CreateResponse(id: String, region: String, bucket: String, parts: List[UploadPart])
  implicit val createResponseFormat: Format[CreateResponse] = Jsonx.formatCaseClass[CreateResponse]
}
