package controllers

import javax.inject.Inject

import com.gu.atom.data.{PublishedDataStore, PreviewDataStore}
import com.gu.atom.play.AtomAPIActions
import com.gu.atom.publish.{PreviewAtomPublisher, LiveAtomPublisher}
import com.gu.pandahmac.HMACAuthActions
import model.UpdatedMetadata
import model.commands.{UpdateMetadataCommand, AddAssetCommand}
import play.api.Configuration
import util.{YouTubeConfig, AWSConfig}
import util.atom.MediaAtomImplicits
import model.commands.CommandExceptions._
import play.api.libs.json._
import data.JsonConversions._

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

  def getAtom(id: String) = APIHMACAuthAction {
    previewDataStore.getAtom(id) match {
      case Some(atom) => Ok(Json.toJson(atom))
      case None => NotFound(jsonError(s"no atom with id $id found"))
    }
  }

  def addAsset(atomId: String) = APIHMACAuthAction { implicit req =>
    req.body.asJson.map { json =>
      try {
        val videoId = (json \ "uri").as[String]
        val mimeType: Option[String] = (json \ "mimeType").asOpt[String]
        val version: Option[Long] = (json \ "version").asOpt[Long]

        AddAssetCommand(atomId, videoId, version, mimeType).process()

        Ok
      } catch {
        commandExceptionAsResult
      }
    }.getOrElse {
      BadRequest("Could not read json")
    }
  }

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
