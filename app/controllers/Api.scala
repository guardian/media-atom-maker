package controllers

import com.gu.media.{Capi, MediaAtomMakerPermissionsProvider}
import com.gu.media.logging.Logging
import com.gu.media.model.{Asset, MediaAtom, MediaAtomBeforeCreation}
import com.gu.media.util.MediaAtomImplicits
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.User
import data.DataStores
import model.WorkflowMediaAtom
import model.commands.CommandExceptions._
import model.commands._
import net.logstash.logback.marker.{LogstashMarker, Markers}
import play.api.Configuration
import util._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

class Api(
    override val stores: DataStores,
    conf: Configuration,
    override val authActions: HMACAuthActions,
    youtube: YouTube,
    awsConfig: AWSConfig,
    override val permissions: MediaAtomMakerPermissionsProvider,
    capi: Capi,
    thumbnailGenerator: ThumbnailGenerator,
    override val controllerComponents: ControllerComponents
) extends MediaAtomImplicits
    with AtomController
    with JsonRequestParsing
    with Logging {

  import authActions.{APIAuthAction, APIHMACAuthAction}

  def allowCORSAccess(methods: String, args: Any*) =
    CORSable(awsConfig.workflowUrl) {
      Action { implicit req =>
        val requestedHeaders = req.headers("Access-Control-Request-Headers")
        NoContent.withHeaders(
          "Access-Control-Allow-Methods" -> methods,
          "Access-Control-Allow-Headers" -> requestedHeaders
        )
      }
    }

  def getMediaAtoms(
      search: Option[String],
      limit: Option[Int],
      shouldUseCreatedDateForSort: Boolean,
      mediaPlatform: Option[String]
  ) = APIAuthAction {
    val atoms = stores.atomListStore.getAtoms(
      search,
      limit,
      shouldUseCreatedDateForSort,
      mediaPlatform
    )
    Ok(Json.toJson(atoms))
  }

  def getMediaAtom(id: String) = APIAuthAction { req =>
    try {
      val maybeCorsValue =
        req.headers.get("Origin").filter(_.endsWith("gutools.co.uk"))
      val atom = getPreviewAtom(id)
      Ok(Json.toJson(MediaAtom.fromThrift(atom))).withHeaders(
        "Access-Control-Allow-Origin" -> maybeCorsValue.getOrElse(""),
        "Access-Control-Allow-Credentials" -> maybeCorsValue.isDefined.toString
      )
    } catch {
      commandExceptionAsResult
    }
  }

  def getPublishedMediaAtom(id: String) = APIAuthAction {
    try {
      val atom = getPublishedAtom(id)
      Ok(Json.toJson(MediaAtom.fromThrift(atom)))
    } catch {
      case CommandException(_, 404) =>
        Ok(Json.obj())

      case err: CommandException =>
        commandExceptionAsResult(err)
    }
  }

  def resetDurationFromActive(id: String) = APIAuthAction { implicit req =>
    val previewAtom = MediaAtom.fromThrift(getPreviewAtom(id))
    val updatedAtom = previewAtom
      .getActiveYouTubeAsset()
      .flatMap(asset => youtube.getDuration(asset.id))
      .map(duration =>
        updateAtom(previewAtom.copy(duration = Some(duration)), req.user)
      )
      .getOrElse(previewAtom)

    Ok(Json.toJson(updatedAtom))
  }

  def publishMediaAtom(id: String) = APIHMACAuthAction.async(parse.empty) {
    implicit req =>
      val command = PublishAtomCommand(
        id,
        stores,
        youtube,
        req.user,
        capi,
        permissions,
        awsConfig,
        thumbnailGenerator
      )

      val updatedAtom: Future[MediaAtom] = command.process()

      updatedAtom.map(updatedAtom => {
        Ok(Json.toJson(updatedAtom))
      }) recover commandExceptionAsResult
  }

  def createMediaAtom = APIAuthAction { implicit req =>
    parse(req) { data: MediaAtomBeforeCreation =>
      val command = CreateAtomCommand(data, stores, req.user)
      val atom = command.process()

      Created(Json.toJson(atom)).withHeaders("Location" -> atomUrl(atom.id))
    }
  }

  def createWorkflowMediaAtom = CORSable(awsConfig.workflowUrl) {
    APIAuthAction { implicit req =>
      parse(req) { workflowMediaAtom: WorkflowMediaAtom =>
        val command =
          CreateWorkflowAtomCommand(workflowMediaAtom, stores, req.user)
        val atom = command.process()
        Created(Json.toJson(atom)).withHeaders("Location" -> atomUrl(atom.id))
      }
    }
  }

  def updateMediaAtom(id: String) = APIAuthAction { implicit req =>
    parse(req) { atom: MediaAtom =>
      val command = UpdateAtomCommand(id, atom, stores, req.user, awsConfig)
      val updatedAtom = command.process()

      Ok(Json.toJson(updatedAtom))
    }
  }

  def addAsset(atomId: String) = APIAuthAction { implicit req =>
    implicit val readCommand: Reads[AddAssetCommand] =
      (JsPath \ "uri").read[String].map { videoUri =>
        AddAssetCommand(atomId, videoUri, stores, youtube, req.user, awsConfig)
      }

    parse(req) { command: AddAssetCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }

  def deleteAsset(atomId: String) = APIAuthAction(parse.json) { implicit req =>
    try {
      val asset = req.body.as[Asset]

      val markers: LogstashMarker = Markers.appendEntries(
        Map(
          "userId" -> req.user.email,
          "atomId" -> atomId,
          "assetId" -> asset.id,
          "assetVersion" -> asset.version
        ).asJava
      )

      log.info(
        markers,
        s"request to delete asset version ${asset.version} on atom $atomId"
      )

      val command =
        DeleteAssetCommand(atomId, asset, stores, req.user, awsConfig)
      val atom = command.process()
      Ok(Json.toJson(atom))
    } catch {
      commandExceptionAsResult
    }
  }

  def deleteAssetList(atomId: String) = APIAuthAction(parse.json) {
    implicit req =>
      try {
        val assets = req.body.as[Seq[Asset]]
        val assetsVersion = assets.map(_.version).mkString(",")

        val markers: LogstashMarker = Markers.appendEntries(
          Map(
            "userId" -> req.user.email,
            "atomId" -> atomId,
            "assetId" -> assets.map(_.id).mkString(","),
            "assetVersion" -> assetsVersion
          ).asJava
        )

        log.info(
          markers,
          s"request to delete asset version ${assetsVersion} on atom $atomId"
        )

        val command =
          DeleteAssetListCommand(atomId, assets, stores, req.user, awsConfig)
        val atom = command.process()
        Ok(Json.toJson(atom))
      } catch {
        commandExceptionAsResult
      }
  }

  private def atomUrl(id: String) = s"/atom/$id"

  def setActiveAsset(atomId: String) = APIAuthAction { implicit req =>
    parse(req) { request: ActivateAssetRequest =>
      val command = ActiveAssetCommand(
        atomId,
        request,
        stores,
        youtube,
        req.user,
        awsConfig,
        new S3ImageUtil(awsConfig)
      )
      val atom = command.process()

      Ok(Json.toJson(atom))
    }
  }

  def deleteAtom(id: String) = CanDeleteAtom { implicit req =>
    try {
      DeleteCommand(id, stores, youtube).process()
      Ok(s"Atom $id deleted")
    } catch {
      commandExceptionAsResult
    }
  }

  private def updateAtom(
      atom: MediaAtom,
      user: User
  ) = UpdateAtomCommand(atom.id, atom, stores, user, awsConfig).process()
}
