package controllers

import javax.inject.Inject

import com.gu.atom.data.{PublishedDataStore, PreviewDataStore}
import com.gu.atom.play.AtomAPIActions
import com.gu.atom.publish.{PreviewAtomPublisher, LiveAtomPublisher}
import com.gu.pandahmac.HMACAuthActions
import model.commands.AddAssetCommand
import play.api.Configuration
import util.AWSConfig
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
                     val authActions: HMACAuthActions)
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


}
