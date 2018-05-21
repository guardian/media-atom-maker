package controllers

import java.io.File

import com.amazonaws.services.s3.model.PutObjectResult
import com.gu.atom.play.AtomAPIActions
import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTubeClaims
import com.gu.media.Capi
import com.gu.pandahmac.HMACAuthActions
import util.{ActivateAssetRequest, YouTube}
import com.gu.media.model.{Asset, MediaAtom, MediaAtomBeforeCreation, PlutoSyncMetadataMessage}
import com.gu.media.util.{MediaAtomHelpers, MediaAtomImplicits}
import data.DataStores
import model.commands.CommandExceptions._
import model.commands._
import model.WorkflowMediaAtom
import play.api.{Configuration, Environment}
import util.{AWSConfig, CORSable}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Api2 (override val stores: DataStores, conf: Configuration, override val authActions: HMACAuthActions,
            youtube: YouTube, awsConfig: AWSConfig,
            override val permissions: MediaAtomMakerPermissionsProvider, capi: Capi, env: Environment)

  extends MediaAtomImplicits
    with AtomAPIActions
    with AtomController
    with JsonRequestParsing
    with Logging {

  import authActions.{APIAuthAction, APIHMACAuthAction}

  def allowCORSAccess(methods: String, args: Any*) = CORSable(awsConfig.workflowUrl) {
    Action { implicit req =>
      val requestedHeaders = req.headers("Access-Control-Request-Headers")
      NoContent.withHeaders("Access-Control-Allow-Methods" -> methods, "Access-Control-Allow-Headers" -> requestedHeaders)
    }
  }

  def getMediaAtoms(search: Option[String], limit: Option[Int]) = APIAuthAction {
    val atoms = stores.atomListStore.getAtoms(search, limit)
    Ok(Json.toJson(atoms))
  }

  def getMediaAtom(id: String) = APIAuthAction {
    try {
      val atom = getPreviewAtom(id)
      Ok(Json.toJson(MediaAtom.fromThrift(atom)))
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

  def publishMediaAtom(id: String) = APIHMACAuthAction.async(parse.empty) { implicit req =>
      val command = PublishAtomCommand(id, stores, youtube, req.user, capi, permissions, awsConfig, env)

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
          val command = CreateWorkflowAtomCommand(workflowMediaAtom, stores, req.user)
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
      val command = DeleteAssetCommand(atomId, asset, stores, req.user, awsConfig)
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
    catch {
      commandExceptionAsResult
    }
  }

  private def atomUrl(id: String) = s"/atom/$id"

  def setActiveAsset(atomId: String) = APIAuthAction { implicit req =>
    parse(req) { request: ActivateAssetRequest =>
      val command = ActiveAssetCommand(atomId, request, stores, youtube, req.user, awsConfig)
      val atom = command.process()

      Ok(Json.toJson(atom))
    }
  }

  def deleteAtom(id: String) = CanDeleteAtom { implicit req =>
    try {
      DeleteCommand(id, stores, youtube).process()
      Ok(s"Atom $id deleted")
    }
    catch {
      commandExceptionAsResult
    }
  }

  def getPlutoAtoms = APIAuthAction {  implicit req =>

    val unprocessedAssetResponses: List[PlutoSyncMetadataMessage] = stores.pluto.list()

    val uploadsWithoutPlutoId = unprocessedAssetResponses.foldRight(Map[String, MediaAtom]())((upload, acc) => {
      if (!acc.contains(upload.atomId)) {
        previewDataStore.getAtom(upload.atomId) match {
          case Right(atom) => {
            val mediaAtom = MediaAtom.fromThrift(atom)

            mediaAtom.plutoData match {
              case Some(plutoData) if plutoData.projectId.isDefined => acc
              case _ => acc ++ Map(upload.atomId -> mediaAtom)
            }
          }
          case Left(error) => {
            log.error(s"Error in fetching atom ${upload.atomId} corresponding to s3Key ${upload.s3Key}" + error.msg)
            acc
          }
        }
      } else acc
    }).values
    Ok(Json.toJson(uploadsWithoutPlutoId))
  }

  def uploadPacFile(id: String) = APIAuthAction(parse.multipartFormData) { request =>
    request.body.file("pac-file").map { file =>
      val atom = getPreviewAtom(id)
      val mediaAtom: MediaAtom = MediaAtom.fromThrift(atom)

      try {
        val pacFileUpload = PacFileUploadCommand(
          mediaAtom,
          file.ref.file,
          stores,
          request.user,
          awsConfig
        ).process()

        Ok(Json.toJson(pacFileUpload))
      }
      catch {
        commandExceptionAsResult
      }
    }.getOrElse(
      BadRequest
    )
  }
}
