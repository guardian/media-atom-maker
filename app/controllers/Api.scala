package controllers

import java.util.Date
import javax.inject._

import com.gu.atom.data._
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.pandomainauth.action.AuthActions
import data.JsonConversions._
import model.ThriftUtil._
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.atom.MediaAtomImplicits
import play.api.libs.json._

import scala.util.{Failure, Success}

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
    dataStore.getMediaAtom(id) match {
      case Some(atom) => Ok(Json.toJson(atom))
      case None => NotFound(jsonError(s"no atom with id $id found"))
    }
  }

  def createMediaAtom = thriftResultAction(atomBodyParser) { implicit req =>
    val atom = req.body
    dataStore.createMediaAtom(atom).fold(
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

  def updateMediaAtom(atomId: String) = thriftResultAction(atomBodyParser) { implicit req =>
    val updatedData = req.body.tdata
    dataStore.getMediaAtom(atomId) match {
      case Some(atom) =>
        val newVersion = {
          val maxVersion = {
            val activeVersion = atom.tdata.activeVersion
            val assetVersions = atom.tdata.assets.map(_.version)
            val versions = activeVersion.map(assetVersions.+:(_)) getOrElse assetVersions
            if (versions.isEmpty) 0
            else versions.max
          }
          maxVersion + 1
        }
        val newAtom = atom
                      .withRevision(newVersion)
                      .updateData { media =>
                        media.copy(
                          activeVersion = Some(newVersion),
                          title = updatedData.title,
                          category = updatedData.category,
                          duration = updatedData.duration
                        )
                      }
        dataStore.updateMediaAtom(newAtom).fold(
          err => InternalServerError(err.msg),
          _ => {
            val event = ContentAtomEvent(newAtom, EventType.Update, now())

            previewPublisher.publishAtomEvent(event) match {
              case Success(_) => NoContent
              case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
            }

            Created(s"Updated atom $atomId").withHeaders("Location" -> atomUrl(atom.id))
          }
        )
      case None => NotFound(s"atom not found $atomId")
    }
  }

  def addAsset(atomId: String) = thriftResultAction(assetBodyParser) { implicit req =>
    val newAsset = req.body

    dataStore.getMediaAtom(atomId) match {
      case Some(atom) =>
        val ma = atom.tdata
        val assets = ma.assets
        val newAtom = atom
          .withData(ma.copy(
                      activeVersion = Some(newAsset.version),
                      assets = newAsset +: assets
                    ))
          .withRevision(_ + 1)

        dataStore.updateMediaAtom(newAtom).fold(
          err => InternalServerError(err.msg),
          _ => {
            
            val event = ContentAtomEvent(newAtom, EventType.Update, now())

            previewPublisher.publishAtomEvent(event) match {
              case Success(_)  => NoContent
              case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
            }

            Created(s"updated atom $atomId").withHeaders("Location" -> atomUrl(atom.id))
          }
        )
      case None => NotFound(s"atom not found $atomId")
    }
  }

  def now() = (new Date()).getTime()

  def publishAtom(atomId: String) = APIAuthAction { implicit req =>
    dataStore.getMediaAtom(atomId) match {
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
    dataStore.getMediaAtom(atomId) match {
      case Some(atom) =>
        if(!atom.tdata.assets.exists(_.version == version)) {
          InternalServerError(jsonError(s"no asset is listed for version $version"))
        } else {
          dataStore.updateMediaAtom(
            atom
              .withRevision(_ + 1)
              .updateData { media => media.copy(activeVersion = Some(version)) }
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
