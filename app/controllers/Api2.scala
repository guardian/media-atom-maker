package controllers

import _root_.util.AWSConfig
import com.gu.atom.play.AtomAPIActions
import com.gu.editorial.permissions.client.PermissionsProvider
import com.gu.media.logging.Logging
import com.gu.media.upload.model.PlutoSyncMetadata
import com.gu.media.youtube.YouTube
import com.gu.pandahmac.HMACAuthActions
import data.DataStores
import model.MediaAtom
import model.commands.CommandExceptions._
import model.commands._
import play.api.Configuration
import util.atom.MediaAtomImplicits
import play.api.libs.json._

class Api2 (override val stores: DataStores, conf: Configuration, override val authActions: HMACAuthActions,
            youTube: YouTube, awsConfig: AWSConfig, override val permissions: PermissionsProvider)

  extends MediaAtomImplicits
    with AtomAPIActions
    with AtomController
    with JsonRequestParsing
    with Logging {

  import authActions.APIHMACAuthAction

  def getMediaAtoms(search: Option[String], limit: Option[Int]) = APIHMACAuthAction {
    val atoms = stores.atomListStore.getAtoms(search, limit)
    Ok(Json.toJson(atoms))
  }

  def getMediaAtom(id: String) = APIHMACAuthAction {
    try {
      val atom = getPreviewAtom(id)
      Ok(Json.toJson(MediaAtom.fromThrift(atom)))
    } catch {
      commandExceptionAsResult
    }
  }

  def getPublishedMediaAtom(id: String) = APIHMACAuthAction {
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

  def publishMediaAtom(id: String) = APIHMACAuthAction { implicit req =>
    try {
      val command = PublishAtomCommand(id, stores, youTube, req.user)

      val updatedAtom = command.process()
      Ok(Json.toJson(updatedAtom))
    } catch {
      commandExceptionAsResult
    }
  }

  def createMediaAtom = APIHMACAuthAction { implicit req =>
    parse(req) { data: CreateAtomCommandData =>
      val command = CreateAtomCommand(data, stores, req.user)
      val atom = command.process()

      Created(Json.toJson(atom)).withHeaders("Location" -> atomUrl(atom.id))
    }
  }

  def putMediaAtom(id: String) = APIHMACAuthAction { implicit req =>
    parse(req) { atom: MediaAtom =>
      val command = UpdateAtomCommand(id, atom, stores, req.user)
      val updatedAtom = command.process()

      Ok(Json.toJson(updatedAtom))
    }
  }

  def addAsset(atomId: String) = CanAddAsset { implicit req =>
    implicit val readCommand: Reads[AddAssetCommand] =
      (JsPath \ "uri").read[String].map { videoUri =>
        AddAssetCommand(atomId, videoUri, stores, youTube, req.user)
      }

    parse(req) { command: AddAssetCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }


  private def atomUrl(id: String) = s"/atom/$id"

  def setActiveAsset(atomId: String) = CanAddAsset { implicit req =>
    implicit val readCommand: Reads[ActiveAssetCommand] =
      (JsPath \ "youtubeId").read[String].map { videoUri =>
        ActiveAssetCommand(atomId, videoUri, stores, youTube, req.user)
      }

    parse(req) { command: ActiveAssetCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }

  def setPlutoId(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val readCommand: Reads[SetPlutoIdCommand] =
      (JsPath \ "plutoId").read[String].map { plutoId =>
        new SetPlutoIdCommand(atomId, plutoId, stores, req.user)
      }

    parse(req) { command: SetPlutoIdCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }

  def getAuditTrailForAtomId(id: String) = APIHMACAuthAction { implicit req =>
    Ok(Json.toJson(auditDataStore.getAuditTrailForAtomId(id)))
  }

  def deleteAtom(id: String) = CanDeleteAtom { implicit req =>
    try {
      DeleteCommand(id, stores).process()
      Ok(s"Atom $id deleted")
    }
    catch {
      commandExceptionAsResult
    }
  }

  def getPlutoAtoms = APIHMACAuthAction {  implicit req =>

    val unprocessedAssetResponses: List[PlutoSyncMetadata] = stores.pluto.list()

    val uploadsWithoutPlutoId = unprocessedAssetResponses.foldRight(Map[String, MediaAtom]())((upload, acc) => {
      if (!acc.contains(upload.atomId)) {
        previewDataStore.getAtom(upload.atomId) match {
          case Right(atom) => {
            val mediaAtom = MediaAtom.fromThrift(atom)
            mediaAtom.plutoProjectId match {
              case None => acc ++ Map(upload.atomId -> mediaAtom)
              case Some(string) => acc
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

  def sendToPluto(id: String) = APIHMACAuthAction { implicit req =>

    implicit val readCommand: Reads[AddPlutoProjectCommand] =

      (JsPath \ "plutoId").read[String].map { plutoId =>
        new AddPlutoProjectCommand(id, plutoId, stores, req.user, awsConfig)
      }

    parse(req) { command: AddPlutoProjectCommand =>
      command.process()
      Ok("Added pluto project to atom")

    }
  }
}
