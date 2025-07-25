package controllers

import com.gu.ai.x.play.json.Encoders._
import com.amazonaws.services.stepfunctions.model.{ExecutionAlreadyExistsException, ExecutionListItem}
import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.logging.Logging
import com.gu.media.model.{ClientAsset, ClientAssetProcessing, MediaAtom, YouTubeAsset}
import com.gu.media.upload.model._
import com.gu.media.util.{MediaAtomHelpers, MediaAtomImplicits}
import com.gu.media.youtube.YouTubeVideos
import com.gu.pandahmac.HMACAuthActions
import data.{DataStores, UnpackedDataStores}
import com.gu.ai.x.play.json.Jsonx
import model.commands.CommandExceptions.commandExceptionAsResult
import model.commands.SubtitleFileUploadCommand
import play.api.libs.Files
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, ControllerComponents, MultipartFormData}
import util._

import scala.annotation.tailrec
import scala.util.control.NonFatal

class UploadController(override val authActions: HMACAuthActions, awsConfig: AWSConfig, stepFunctions: StepFunctions,
                       override val stores: DataStores, override val permissions: MediaAtomMakerPermissionsProvider,
                       youTube: YouTubeVideos, override val controllerComponents: ControllerComponents)

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

  /**
   * def uploadSubtitleFile(atomId: String, version: Int)
   * @param atomId
   * @param version
   * @return
   *
   * - new method based on Api.uploadPacFile() and UploadController.create()
   * - see how PacFileUploadCommand uploads the file to S3
   * - PacFileUploadCommand also notifies Pluto - what, if anything, do we have to tell Pluto?
   * - this method would also be the trigger for the reprocessing in the state machine
   * - the state machine would be responsible for updating the models (media atom & pipeline cache) to include the
   *     subtitle source asset and m3u8 output asset
   * - should we modify the existing state machine or create a new one?  Considerations would be
   *    - first state machine step is to save the upload (pipeline cache) model to dynamo - in our case the model, keyed
   *       on atom id and version, will already exist and we want to modify it to add the subtitle file as a source asset.
   *    - many of the initial steps are concerned with waiting for the multipart video upload, so we would want to skip
   *       to the transcoding step
   *
   */
  def uploadSubtitleFile(atomId: String, version: Int): Action[MultipartFormData[Files.TemporaryFile]] =
    LookupPermissions(parse.multipartFormData) { implicit request =>
      // TODO: check permissions
      request.body.file("subtitle-file").map { file =>
        val atom = getPreviewAtom(atomId)
        val mediaAtom: MediaAtom = MediaAtom.fromThrift(atom)

        try {
          SubtitleFileUploadCommand(
            mediaAtom,
            version,
            file,
            stores,
            request.user,
            awsConfig
          ).process()

          Ok(Json.parse("{}"))
        }
        catch {
          commandExceptionAsResult
        }
    }.getOrElse(
      BadRequest
    )
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
      try {
        val status = youTube.getProcessingStatus(id).map(ClientAssetProcessing(_))
        video.copy(processing = status)
      } catch {
        case NonFatal(e) =>
          log.error(s"Unable to get YouTube status for ${video.id}", e)
          video
      }

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
