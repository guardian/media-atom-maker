package controllers

import _root_.util.{AWSConfig}
import com.gu.atom.play.AtomAPIActions
import com.gu.media.upload.PlutoSyncMetadata
import com.gu.media.youtube.YouTube
import com.gu.pandahmac.HMACAuthActions
import data.DataStores
import model.Category.Hosted
import model.MediaAtom
import model.commands.CommandExceptions._
import model.commands._
import play.api.{Configuration, Logger}
import util.atom.MediaAtomImplicits
import play.api.libs.json._

class Api2 (override val stores: DataStores, conf: Configuration, val authActions: HMACAuthActions,
            youTube: YouTube, awsConfig: AWSConfig)

  extends MediaAtomImplicits
    with AtomAPIActions
    with AtomController
    with JsonRequestParsing {

  import authActions.APIHMACAuthAction

  def getMediaAtoms = APIHMACAuthAction {
    def created(atom: MediaAtom) = atom.contentChangeDetails.created.map(_.date.getMillis)

    previewDataStore.listAtoms.fold(
      err =>   InternalServerError(jsonError(err.msg)),
      atoms => {
        // TODO add `Hosted` category.
        // Although `Hosted` is a valid category, the APIs driving the React frontend perform authenticated calls to YT.
        // These only work with content that we own. `Hosted` can have third-party assets so the API calls will fail.
        // Add `Hosted` once the UI is smarter and removes features when category is `Hosted`.
        val mediaAtoms = atoms.map(MediaAtom.fromThrift)
          .toList
          .filter(_.category != Hosted)
          .sortBy(created)
          .reverse // newest atoms first

        Ok(Json.toJson(mediaAtoms))
      }
    )
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

  def addAsset(atomId: String) = APIHMACAuthAction { implicit req =>
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

  def setActiveAsset(atomId: String) = APIHMACAuthAction { implicit req =>
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

  def deleteAtom(id: String) = APIHMACAuthAction {
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
            Logger.error(s"Error in fetching atom ${upload.atomId} corresponding to s3Key ${upload.s3Key}" + error.msg)
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
