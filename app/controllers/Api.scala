package controllers

import com.gu.contentatom.thrift.{ ContentAtomEvent, EventType }
import com.gu.contentatom.thrift.atom.quiz.Asset
import com.gu.contentatom.thrift.{ Atom, AtomData }
import javax.inject._
import java.util.{ Date, UUID }
import play.api.libs.json._
import play.api.mvc._
import model.ThriftUtil
import ThriftUtil._
import data._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.util.{ Success, Failure }

import data.JsonConversions._

class Api @Inject() (val dataStore: DataStore, val publisher: AtomPublisher) extends AtomController {

  // takes a configured URL object and shows how it would look as a content atom

  def getMediaAtom(id: String) = Action { implicit req =>
    dataStore.getMediaAtom(id) match {
      case Some(atom) => Ok(Json.toJson(atom))
      case None => NotFound(jsonError(s"no atom with id $id found"))
    }
  }

  def createMediaAtom = thriftResultAction(atomBodyParser) { implicit req =>
      val atom = req.body
      try {
        dataStore.createMediaAtom(atom)
        Created(Json.toJson(atom)).withHeaders("Location" -> s"/atom/${atom.id}")
      } catch {
        case data.IDConflictError => Conflict(s"${atom.id} already exists")
      }
    }

  def addAsset(atomId: String) = thriftResultAction(assetBodyParser) { implicit req =>
    val newAsset = req.body
    dataStore.getMediaAtom(atomId) match {
      case Some(atom) =>
        val data = atom.dataAs[AtomData.Media]
        val ma = data.media
        val assets = ma.assets
        val newAtom = atom.copy(
          contentChangeDetails = atom.contentChangeDetails.copy(
            revision = atom.contentChangeDetails.revision + 1
          ),
          data = data.copy(
            media = ma.copy(
              activeVersion = newAsset.version,
              assets = newAsset +: assets
            )
          )
        )
        try {
          dataStore.updateMediaAtom(newAtom)
          Created(s"updated atom $atomId")
        } catch {
          case err: VersionConflictError => InternalServerError(err.msg)
        }
      case None => NotFound(s"atom not found $atomId")
    }
  }

  def now() = (new Date()).getTime()

  def publishAtom(atomId: String) = Action { implicit req =>
    dataStore.getMediaAtom(atomId) match {
      case Some(atom) =>
        val event = ContentAtomEvent(atom, EventType.Update, now())
        publisher.publishAtomEvent(event) match {
          case Success(_)  => NoContent
          case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
        }
      case None => NotFound(jsonError(s"No such atom $atomId"))
    }
  }

  // TODO -> this needs to handle paging
  def listAtoms = Action { implicit req =>
    try {
      Ok(Json.toJson(dataStore.listAtoms.toList))
    } catch {
      case err: DataStoreError => InternalServerError(jsonError(err.msg))
    }
  }
}
