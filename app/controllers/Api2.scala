package controllers

import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.action.UserRequest
import data.DataStores
import model.Category.Hosted
import model.MediaAtom
import model.commands.CommandExceptions._
import model.commands._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{AnyContent, Result}
import com.gu.media.youtube.YouTubeVideos
import _root_.util.ExpiryPoller

class Api2 (override val stores: DataStores, val authActions: HMACAuthActions, youTube: YouTubeVideos)
  extends AtomController {

  import authActions.APIHMACAuthAction

  def getMediaAtoms = APIHMACAuthAction {
    def created(atom: MediaAtom) = atom.contentChangeDetails.created.map(_.date.getMillis)

    stores.preview.listAtoms.fold(
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
    stores.preview.getAtom(id) match {
      case Some(atom) => Ok(Json.toJson(MediaAtom.fromThrift(atom)))
      case None => NotFound(jsonError(s"no atom with id $id found"))
    }
  }

  def getPublishedMediaAtom(id: String) = APIHMACAuthAction {
    stores.published.getAtom(id) match {
      case Some(atom) => Ok(Json.toJson(MediaAtom.fromThrift(atom)))
      case None => Ok(Json.obj())
    }
  }

  def publishMediaAtom(id: String) = APIHMACAuthAction { implicit req =>
    try {
      val updatedAtom = PublishAtomCommand(id, req.user, stores, youTube).process()
      Ok(Json.toJson(updatedAtom))
    } catch {
      commandExceptionAsResult
    }
  }

  def createMediaAtom = APIHMACAuthAction { implicit req =>
    parse(req) { command: CreateAtomCommandData =>
      val atom = CreateAtomCommand(command, req.user, stores).process()
      Created(Json.toJson(atom)).withHeaders("Location" -> atomUrl(atom.id))
    }
  }

  def putMediaAtom(id: String) = APIHMACAuthAction { implicit req =>
    parse(req) { atom: MediaAtom =>
      val thriftAtom = atom.asThrift
      val atomWithExpiryChecked = ExpiryPoller.updateStatusIfExpired(thriftAtom, youTube) match {
        case Some(expiredAtom) => expiredAtom
        case _ => atom
      }

      val updatedAtom = UpdateAtomCommand(id, atomWithExpiryChecked, req.user, stores).process()
      Ok(Json.toJson(updatedAtom))
    }
  }

  def addAsset(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val readCommand: Reads[AddAssetCommand] =
      (JsPath \ "uri").read[String].map(AddAssetCommand(atomId, _, req.user, stores, youTube))

    parse(req) { command: AddAssetCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }


  private def atomUrl(id: String) = s"/atom/$id"

  def setActiveAsset(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val readCommand: Reads[ActiveAssetCommand] =
      (JsPath \ "youtubeId").read[String].map(ActiveAssetCommand(atomId, _, req.user, stores, youTube))

    parse(req) { command: ActiveAssetCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }

  def setPlutoId(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val readCommand: Reads[SetPlutoIdCommand] =
      (JsPath \ "plutoId").read[String].map(new SetPlutoIdCommand(atomId, _, req.user, stores))

    parse(req) { command: SetPlutoIdCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }

  def getAuditTrailForAtomId(id: String) = APIHMACAuthAction { implicit req =>
    Ok(Json.toJson(stores.audit.getAuditTrailForAtomId(id)))
  }

  private def parse[T](raw: UserRequest[AnyContent])(fn: T => Result)(implicit reads: Reads[T]): Result = try {
    raw.body.asJson match {
      case Some(rawJson) =>
        rawJson.validate[T] match {
          case JsSuccess(request, _) =>
            fn(request)

          case JsError(errors) =>
            val errorsByPath = errors.flatMap { case(p, e) => e.map(p -> _) } // flatten
            val msg = errorsByPath.map { case(p, e) => s"$p -> $e" }.mkString("\n")

            Logger.info(s"Error parsing request: $msg - ${raw.body}")
            BadRequest(msg)
        }

      case None =>
        Logger.info(s"Error parsing request: ${raw.body}")
        BadRequest("Unable to parse body as JSON")
    }
  } catch {
    commandExceptionAsResult
  }
}
