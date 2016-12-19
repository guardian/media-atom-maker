package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import com.gu.atom.data.{PreviewDataStore, PublishedDataStore}
import com.gu.atom.play.AtomAPIActions
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.pandahmac.HMACAuthActions
import data.JsonConversions._
import data.AuditDataStore
import model.Category.Hosted
import model.commands.CommandExceptions._
import model.commands._
import play.api.Configuration
import model.commands.CommandExceptions._
import util.{ YouTubeConfig, AWSConfig, ExpiryPoller}
import util.atom.MediaAtomImplicits
import play.api.libs.json._
import model.{MediaAtom, UpdatedMetadata}

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
    req.body.asJson.map { json =>
      try {
        val request = json.as[CreateAtomCommandData]

        val atom = CreateAtomCommand(request).process()
        Created(Json.toJson(atom)).withHeaders("Location" -> atomUrl(atom.id))

      } catch {
        commandExceptionAsResult
      }

    }.getOrElse {
      BadRequest("Could not read json")
    }
  }

  def putMediaAtom(id: String) = APIHMACAuthAction { implicit req =>
    implicit val user = req.user
    req.body.asJson.map { json =>
      try {
        val atom = json.as[MediaAtom]
        val updatedAtom = UpdateAtomCommand(id, atom).process()
        Ok(Json.toJson(updatedAtom))
      } catch {
        commandExceptionAsResult
      }
    }.getOrElse {
      BadRequest("Could not read json")
    }
  }

  def addAsset(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val user = req.user

    req.body.asJson.map { json =>
      try {
        val videoId = (json \ "uri").as[String]
        val mimeType: Option[String] = (json \ "mimeType").asOpt[String]
        val version: Option[Long] = (json \ "version").asOpt[Long]

        val atom = AddAssetCommand(atomId, videoId, version, mimeType).process()

        Ok(Json.toJson(atom))
      } catch {
        commandExceptionAsResult
      }
    }.getOrElse {
      BadRequest("Could not read json")
    }
  }


  private def atomUrl(id: String) = s"/atom/$id"

  def updateMetadata(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val user = req.user
    req.body.asJson.map { json =>
      json.validate[UpdatedMetadata] match {
        case JsSuccess(metadata, _) =>
          UpdateMetadataCommand(atomId, metadata).process()
          Ok
        case JsError(e) => BadRequest(s"Json doesn't contain the right fields. $e")
      }

    }.getOrElse {
      BadRequest("Could not read json")
    }
  }

  def setActiveAsset(atomId: String) = APIHMACAuthAction { implicit req =>
    implicit val user = req.user
    req.body.asJson.map { json =>
      try {
        val videoId = (json \ "youtubeId").as[String]
        val atom = ActiveAssetCommand(atomId, videoId).process()
        Ok(Json.toJson(atom))
      } catch {
        commandExceptionAsResult
      }
    }.getOrElse {
      BadRequest("Could not read json")
    }
  }

  def getAuditTrailForAtomId(id: String) = APIHMACAuthAction { implicit req =>
    Ok(Json.toJson(auditDataStore.getAuditTrailForAtomId(id)))
  }
}
