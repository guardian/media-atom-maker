package controllers

import javax.inject.Inject

import _root_.util.{AWSConfig, ExpiryPoller, YouTubeConfig, YouTubeVideoUpdateApi}
import akka.actor.ActorSystem
import com.gu.atom.data.{PreviewDataStore, PublishedDataStore}
import com.gu.atom.play.AtomAPIActions
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.action.UserRequest
import data.AuditDataStore
import model.Category.Hosted
import model.commands.CommandExceptions._
import model.commands._
import model.{MediaAtom, UpdatedMetadata}
import play.api.{Configuration, Logger}
import util.atom.MediaAtomImplicits
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.{AnyContent, Result}

class Api2 @Inject() (implicit val previewDataStore: PreviewDataStore,
                     implicit val publishedDataStore: PublishedDataStore,
                     val livePublisher: LiveAtomPublisher,
                     implicit val previewPublisher: PreviewAtomPublisher,
                     val conf: Configuration,
                     val awsConfig: AWSConfig,
                     val authActions: HMACAuthActions,
                     val youtubeConfig: YouTubeConfig,
                     implicit val auditDataStore: AuditDataStore,
                     val expiryPoller: ExpiryPoller,
                      val system: ActorSystem)

  extends MediaAtomImplicits
    with AtomAPIActions
    with AtomController {

  import authActions.APIHMACAuthAction

  initialize()

  def initialize() = {
    expiryPoller.start(system.scheduler)
  }

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
    previewDataStore.getAtom(id) match {
      case Some(atom) => Ok(Json.toJson(MediaAtom.fromThrift(atom)))
      case None => NotFound(jsonError(s"no atom with id $id found"))
    }
  }

  def getPublishedMediaAtom(id: String) = APIHMACAuthAction {
    publishedDataStore.getAtom(id) match {
      case Some(atom) => Ok(Json.toJson(MediaAtom.fromThrift(atom)))
      case None => Ok(Json.obj())
    }
  }

  def publishMediaAtom(id: String) = APIHMACAuthAction { implicit req =>
    implicit val user = req.user

    try {
      val updatedAtom = PublishAtomCommand(id).process()
      Ok(Json.toJson(updatedAtom))
    } catch {
      commandExceptionAsResult
    }
  }

  def createMediaAtom = APIHMACAuthAction { implicit req =>
    implicit val user = req.user

    parse(req) { command: CreateAtomCommandData =>
      val atom = CreateAtomCommand(command).process()
      Created(Json.toJson(atom)).withHeaders("Location" -> atomUrl(atom.id))
    }
  }

  def putMediaAtom(id: String) = APIHMACAuthAction { implicit req =>
    implicit val user = req.user

    parse(req) { atom: MediaAtom =>
      val thriftAtom = atom.asThrift
      val atomWithExpiryChecked = YouTubeVideoUpdateApi(youtubeConfig).updateStatusIfExpired(thriftAtom) match {
        case Some(expiredAtom) => expiredAtom
        case _ => atom
      }

      val updatedAtom = UpdateAtomCommand(id, atomWithExpiryChecked).process()
      Ok(Json.toJson(updatedAtom))
    }
  }

  def addAsset(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val user = req.user

    implicit val readCommand: Reads[AddAssetCommand] = (JsPath \ "uri").read[String].map(AddAssetCommand(atomId, _))

    parse(req) { command: AddAssetCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }


  private def atomUrl(id: String) = s"/atom/$id"

  def setActiveAsset(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val user = req.user

    implicit val readCommand: Reads[ActiveAssetCommand] =
      (JsPath \ "youtubeId").read[String].map(ActiveAssetCommand(atomId, _))

    parse(req) { command: ActiveAssetCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }

  def setPlutoId(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val user = req.user

    implicit val readCommand: Reads[SetPlutoIdCommand] =
      (JsPath \ "plutoId").read[String].map(new SetPlutoIdCommand(atomId, _))

    parse(req) { command: SetPlutoIdCommand =>
      val atom = command.process()
      Ok(Json.toJson(atom))
    }
  }

  def getAuditTrailForAtomId(id: String) = APIHMACAuthAction { implicit req =>
    Ok(Json.toJson(auditDataStore.getAuditTrailForAtomId(id)))
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
