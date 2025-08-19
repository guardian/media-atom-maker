package controllers

import com.gu.ai.x.play.json.Encoders._
import com.amazonaws.services.stepfunctions.model.{ExecutionAlreadyExistsException, ExecutionListItem}
import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.logging.Logging
import com.gu.media.model.{ClientAsset, ClientAssetProcessing, MediaAtom, VideoSource, YouTubeAsset}
import com.gu.media.upload.model._
import com.gu.media.util.{MediaAtomHelpers, MediaAtomImplicits}
import com.gu.media.youtube.YouTubeVideos
import com.gu.pandahmac.HMACAuthActions
import data.{DataStores, UnpackedDataStores}
import com.gu.ai.x.play.json.Jsonx
import model.commands.CommandExceptions.commandExceptionAsResult
import model.commands.{SubtitleFileDeleteCommand, SubtitleFileUploadCommand}
import org.scanamo.Table
import org.scanamo.generic.auto._
import play.api.libs.Files
import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, MultipartFormData, Result}
import util._

import java.time.Instant
import scala.annotation.tailrec
import scala.util.control.NonFatal


class UploadController(override val authActions: HMACAuthActions, awsConfig: AWSConfig, stepFunctions: StepFunctions,
                       override val stores: DataStores, override val permissions: MediaAtomMakerPermissionsProvider,
                       youTube: YouTubeVideos, override val controllerComponents: ControllerComponents)

  extends AtomController with Logging with JsonRequestParsing with UnpackedDataStores with MediaAtomImplicits {

  import authActions.APIAuthAction

  private val credsGenerator = new CredentialsGenerator(awsConfig)
  private val uploadDecorator = new UploadDecorator(awsConfig, stepFunctions)

  /**
   * prepares a list of CLientAssets that represent the multiple versioned assets for the atom to be displayed in the client.
   * The list is made up of existing assets in the atom (atomAssets) plus assets derived from any state machine upload
   * jobs that are currently running (jobAssets).
   * If an upload is running for an existing asset, then the asset info is merged into the job asset so that the job
   * progress can be seen in the client.
   * Finally, the two sets of assets are deduplicated, so there is one asset per version and sorted so that newest
   * assets are first.
   * @param atomId
   * @return
   */
  def list(atomId: String): Action[AnyContent] = APIAuthAction { req =>
    val atom = MediaAtom.fromThrift(getPreviewAtom(atomId))
    val withStatus = ClientAsset.fromAssets(atom.assets).map(addYouTubeStatus)
    val atomAssets = withStatus.map { asset => uploadDecorator.addMetadataAndSources(atom.id, asset) }

    val jobs = stepFunctions.getJobs(atomId)
    val jobAssets = jobs.flatMap(getRunning(atomAssets, atomId, _))

    // if multiple jobs for same version, only keep the latest
    val jobsByVersion = jobAssets.groupBy(_.id)
    val latestJobAssets = jobsByVersion.map { case (_, job) => job.maxBy(startTime) }.toList

    // remove any atom assets that are represented by running/recent job assets
    val jobVersions = jobsByVersion.keys.toSet
    val filteredAtomAssets = atomAssets.filterNot( asset => jobVersions.contains(asset.id))

    // sort newest version first
    val clientAssets = (latestJobAssets ++ filteredAtomAssets).sortBy(ClientAsset.getVersion).reverse

    Ok(Json.toJson(clientAssets))
  }

  def create: Action[AnyContent] = LookupPermissions { implicit raw =>
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

  def credentials(id: String, key: String): Action[AnyContent] = LookupPermissions { implicit req =>
    getPart(id, key) match {
      case Some(part) =>
        val credentials = credsGenerator.forKey(part.key)
        Ok(Json.toJson(credentials))

      case None =>
        NotFound
    }
  }

  def uploadSubtitleFile(atomId: String, version: Int): Action[MultipartFormData[Files.TemporaryFile]] =
    LookupPermissions(parse.multipartFormData) { implicit req =>
      if (!req.permissions.addSubtitles) {
        Unauthorized(s"User ${req.user.email} is not authorised to upload subtitle asset")
      } else {
        log.info(s"Request to upload subtitle file under atom=$atomId version=$version")
        val result = for {
          file <- req.body.file("subtitle-file")
          upload <- uploadDecorator.getUpload(s"$atomId-$version")
        } yield
          try {
            val subtitleSource = SubtitleFileUploadCommand(
              upload,
              file,
              stores,
              req.user,
              awsConfig
            ).process()

            // add subtitle file to upload asset's list of sources
            val rerunUpload = UploadBuilder.buildForSubtitleChange(upload, Some(subtitleSource))

            // reprocessing will also save the upload to the DB, but saving it here first improves UI responsiveness
            saveUploadToDb(rerunUpload)

            processSubtitleChange(rerunUpload)

            log.info(s"Upload being reprocessed after subtitle upload ${upload.id}")
            Ok(Json.toJson(rerunUpload))
          }
          catch {
            commandExceptionAsResult
          }

        result.getOrElse(
          BadRequest
        )
      }
    }

  def deleteSubtitleFile(atomId: String, version: Int): Action[AnyContent] =
    LookupPermissions { implicit req =>
      if (!req.permissions.addSubtitles) {
        Unauthorized(s"User ${req.user.email} is not authorised to upload subtitle asset")
      } else {
        log.info(s"Request to delete subtitle file under atom=$atomId version=$version")
        uploadDecorator.getUpload(s"$atomId-$version") match {
          case Some(upload) =>
            try {
              SubtitleFileDeleteCommand(
                upload,
                stores,
                req.user,
                awsConfig
              ).process()

              // remove subtitle files from upload asset's list of sources
              val rerunUpload = UploadBuilder.buildForSubtitleChange(upload, None)

              // reprocessing will also save the upload to the DB, but saving it here first improves UI responsiveness
              saveUploadToDb(rerunUpload)

              processSubtitleChange(rerunUpload)

              log.info(s"Upload being reprocessed after subtitle deletion ${upload.id}")
              Ok(Json.toJson(rerunUpload))
            }
            catch {
              commandExceptionAsResult
            }
          case _ =>
            NotFound
        }
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

  /**
   * re-run an existing upload through the state machine to reprocess the video after subtitle changes
   * @param upload
   * @return
   */
  private def processSubtitleChange(upload: Upload): Unit = {
    val executionName = s"${upload.id}.${Upload.getCurrentSubtitleVersion(upload)}"
    stepFunctions.start(upload, Some(executionName))
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

  private def getRunning(assets: List[ClientAsset], atomId: String, job: ExecutionListItem): Option[ClientAsset] = {
    val existingAsset = assets.find { asset =>
      val version = asset.id
      job.getName.contains(s"$atomId-$version")
    }

    val events = stepFunctions.getEventsInReverseOrder(job)
    val startTimestamp = job.getStartDate.getTime

    val upload = stepFunctions.getTaskEntered(events)
    val error = stepFunctions.getExecutionFailed(events)

    val jobAsAsset = upload.map { case (state, upload) =>
      ClientAsset.fromUpload(state, startTimestamp, upload, error)
    }

    existingAsset match {
      case Some(asset) =>
        // merge job's processing info with existing asset
        jobAsAsset.map(_.copy(id = asset.id, asset = asset.asset))
      case None =>
        // return the running job as an asset
        jobAsAsset.map( asset => asset.copy(id = asset.id.split("-").last))
    }
  }

  private def startTime(asset: ClientAsset): Long = asset.metadata.flatMap(_.startTimestamp).getOrElse(0)

  private def saveUploadToDb(upload: Upload): Unit = {
    val table = Table[Upload](awsConfig.cacheTableName)
    awsConfig.scanamo.exec(table.put(upload))
  }
}

object UploadController {
  case class CreateResponse(id: String, region: String, bucket: String, parts: List[UploadPart])
  implicit val createResponseFormat: Format[CreateResponse] = Jsonx.formatCaseClass[CreateResponse]
}
