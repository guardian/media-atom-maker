package controllers

import javax.inject.Inject

import com.gu.atom.data.{PreviewDataStore, PublishedDataStore}
import com.gu.atom.play.AtomAPIActions
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.contentatom.thrift.Atom
import com.gu.contentatom.thrift.atom.media.MediaAtom
import com.gu.pandahmac.HMACAuthActions
import data.JsonConversions._
import model.commands.CommandExceptions._
import model.commands._
import play.api.Configuration
import util.AWSConfig
import util.atom.MediaAtomImplicits
import play.api.libs.json._

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

  def putMediaAtom(id: String) = APIHMACAuthAction { implicit req =>
    req.body.asJson.map { json =>
      try {
        val atom = json.as[Atom]
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
