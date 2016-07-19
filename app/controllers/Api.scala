package controllers

import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.contentatom.thrift.{ ContentAtomEvent, EventType }
import com.gu.pandomainauth.action.AuthActions

import play.api.Configuration

import util.atom.MediaAtomImplicits

import javax.inject._
import play.api.libs.json._
import model.ThriftUtil
import ThriftUtil._
import com.gu.atom.data._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.util.{ Success, Failure }
import java.util.Date

import data.JsonConversions._

class Api @Inject() (val dataStore: DataStore,
                     val livePublisher: LiveAtomPublisher,
                     val previewPublisher: PreviewAtomPublisher,
                     val conf: Configuration,
                     val authActions: AuthActions)
    extends AtomController
    with MediaAtomImplicits {

  import authActions.APIAuthAction

  private def atomUrl(id: String) = s"/atom/$id"

  // takes a configured URL object and shows how it would look as a content atom

  def getMediaAtom(id: String) = APIAuthAction { implicit req =>
    dataStore.getAtom(id) match {
      case Some(atom) => Ok(Json.toJson(atom))
      case None => NotFound(jsonError(s"no atom with id $id found"))
    }
  }

  def createMediaAtom = thriftResultAction(atomBodyParser) { implicit req =>
    val atom = req.body
    dataStore.createAtom(atom).fold(
      { case IDConflictError =>
        Conflict(s"${atom.id} already exists")
        case _ => InternalServerError("Unknown error")
      },
      _ => {
        val event = ContentAtomEvent(atom, EventType.Update, now())

        previewPublisher.publishAtomEvent(event) match {
          case Success(_)  => NoContent
          case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
        }

        Created(Json.toJson(atom))
          .withHeaders("Location" -> atomUrl(atom.id))
      }
    )
  }

  def addAsset(atomId: String) = thriftResultAction(assetBodyParser) { implicit req =>
    val newAsset = req.body

    dataStore.getAtom(atomId) match {
      case Some(atom) =>
        val ma = atom.tdata
        val assets = ma.assets
        val newAtom = atom
          .withData(ma.copy(
                      activeVersion = newAsset.version,
                      assets = newAsset +: assets
                    ))
          .withRevision(_ + 1)

        dataStore.updateAtom(newAtom).fold(
          err => InternalServerError(err.msg),
          _ => {
            
            val event = ContentAtomEvent(newAtom, EventType.Update, now())

            previewPublisher.publishAtomEvent(event) match {
              case Success(_)  => Created(s"updated atom $atomId").withHeaders("Location" -> atomUrl(atom.id))
              case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))

            }
          }
        )
      case None => NotFound(s"atom not found $atomId")
    }
  }

  def now() = (new Date()).getTime()

  def publishAtom(atomId: String) = APIAuthAction { implicit req =>
    dataStore.getAtom(atomId) match {
      case Some(atom) =>
        val event = ContentAtomEvent(atom, EventType.Update, now())
        livePublisher.publishAtomEvent(event) match {
          case Success(_)  => NoContent
          case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
        }
      case None => NotFound(jsonError(s"No such atom $atomId"))
    }
  }

  def revertAtom(atomId: String, version: Long) = APIAuthAction { implicit req =>
    dataStore.getAtom(atomId) match {
      case Some(atom) =>
        if(!atom.tdata.assets.exists(_.version == version)) {
          InternalServerError(jsonError(s"no asset is listed for version $version"))
        } else {
          dataStore.updateAtom(
            atom
              .withRevision(_ + 1)
              .updateData { media => media.copy(activeVersion = version) }
          )
          Ok(s"updated to $version")
        }
      case None => NotFound(s"atom not found $atomId")
    }
  }

  // TODO -> this needs to handle paging
  def listAtoms = APIAuthAction { implicit req =>
    dataStore.listAtoms.fold(
      err =>   InternalServerError(jsonError(err.msg)),
      atoms => Ok(Json.toJson(atoms.toList))
    )
  }
}
