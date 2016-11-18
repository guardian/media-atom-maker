package controllers

import javax.inject.Inject

import com.gu.atom.data.{PreviewDataStore, PublishedDataStore}
import com.gu.atom.play.AtomAPIActions
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.pandahmac.HMACAuthActions
import data.JsonConversions._
import model.commands.CommandExceptions._
import model.commands._
import model.UpdatedMetadata
import play.api.Configuration
import util.{YouTubeConfig, AWSConfig}
import util.atom.MediaAtomImplicits
import play.api.libs.json._
import model.MediaAtom

class Api2 @Inject() (implicit val previewDataStore: PreviewDataStore,
                     val publishedDataStore: PublishedDataStore,
                     val livePublisher: LiveAtomPublisher,
                     implicit val previewPublisher: PreviewAtomPublisher,
                     val conf: Configuration,
                     val awsConfig: AWSConfig,
                     val authActions: HMACAuthActions,
                     val youtubeConfig: YouTubeConfig)
  extends MediaAtomImplicits
    with AtomAPIActions
    with AtomController {

  import authActions.APIHMACAuthAction

  def getMediaAtoms = APIHMACAuthAction {
    previewDataStore.listAtoms.fold(
      err =>   InternalServerError(jsonError(err.msg)),
      atoms => Ok(Json.toJson(atoms.map(MediaAtom.fromThrift).toList))
    )
  }

  def getMediaAtom(id: String) = APIHMACAuthAction {
    previewDataStore.getAtom(id) match {
      case Some(atom) => Ok(Json.toJson(MediaAtom.fromThrift(atom)))
      case None => NotFound(jsonError(s"no atom with id $id found"))
    }
  }

  def createMediaAtom = APIHMACAuthAction { implicit req =>
    req.body.asJson.map { json =>
      try {
        val atom = CreateAtomCommand(json.as[CreateAtomCommandData]).process()
        Created(Json.toJson(atom)).withHeaders("Location" -> atomUrl(atom.id))

      } catch {
        commandExceptionAsResult
      }

    }.getOrElse {
      BadRequest("Could not read json")
    }
  }

  def putMediaAtom(id: String) = APIHMACAuthAction { implicit req =>
    req.body.asJson.map { json =>
      try {
        val atom = json.as[MediaAtom]
        UpdateAtomCommand(id, atom).process()

        Ok
      } catch {
        commandExceptionAsResult
      }
    }.getOrElse {
      BadRequest("Could not read json")
    }
  }

  def addAsset(atomId: String) = APIHMACAuthAction { implicit req =>
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
    Ok
  }
}
