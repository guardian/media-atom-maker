package controllers

import java.util.Date

import com.gu.atom.data._
import com.gu.atom.play._
import com.gu.contentatom.thrift.atom.media.Category.Hosted
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import com.gu.pandahmac.HMACAuthActions
import data.DataStores
import data.JsonConversions._
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.AWSConfig
import util.ThriftUtil._
import util.atom.MediaAtomImplicits
import play.api.libs.json._
import play.api.mvc.Result

import scala.util.{Failure, Success}

class Api (override val stores: DataStores,
           val conf: Configuration,
           val awsConfig: AWSConfig,
           val authActions: HMACAuthActions)
    extends AtomController
    with MediaAtomImplicits
    with AtomAPIActions {

  import authActions.APIAuthAction

  private def atomUrl(id: String) = s"/atom/$id"

  // takes a configured URL object and shows how it would look as a content atom

  def getMediaAtom(id: String) = APIAuthAction { implicit req =>
    getAtom(id, previewDataStore) { atom =>
      Ok(Json.toJson(atom))
    }
  }

  def getPublishedMediaAtom(id: String) = APIAuthAction { implicit req =>
    getAtom(id, publishedDataStore) { atom =>
      Ok(Json.toJson(atom))
    }
  }

  def createMediaAtom = thriftResultAction(atomBodyParser) { implicit req =>
    val atom = req.body
    previewDataStore.createAtom(atom).fold(
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

    getAtom(atomId, previewDataStore) { atom =>
      val activeVersion = atom.tdata.activeVersion getOrElse {
        val versions = atom.tdata.assets.map(_.version)
        if (versions.isEmpty) 1 else versions.max
      }

      val newAtom = atom
        .withRevision(_ + 1)
        .updateData { media =>
          media.copy(
            activeVersion = Some(activeVersion),
            title = updatedData.title,
            category = updatedData.category,
            duration = updatedData.duration,
            posterUrl = updatedData.posterUrl
          )
        }
      previewDataStore.updateAtom(newAtom).fold(
        err => InternalServerError(err.msg),
        _ => {
          val event = ContentAtomEvent(newAtom, EventType.Update, now())

          previewPublisher.publishAtomEvent(event) match {
            case Success(_) => NoContent
            case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
          }

          Ok(Json.toJson(atom))
        }
      )
    }
  }

  def addAsset(atomId: String) = thriftResultAction(assetBodyParser) { implicit req =>
    val newAsset = req.body

    getAtom(atomId, previewDataStore) { atom =>
      val ma = atom.tdata
      val assets = ma.assets

      if (assets.exists(asset => {
        asset.version == newAsset.version && asset.mimeType == newAsset.mimeType
      })) {
        InternalServerError("could not add asset to atom: version conflict")
      } else {
        val newAtom = atom
          .withData(ma.copy(
            activeVersion = Some(newAsset.version),
            assets = newAsset +: assets
          ))
          .withRevision(_ + 1)

        previewDataStore.updateAtom(newAtom).fold(
          err => InternalServerError(err.msg),
          _ => {

            val event = ContentAtomEvent(newAtom, EventType.Update, now())

            previewPublisher.publishAtomEvent(event) match {
              case Success(_) => NoContent
              case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
            }

            Ok(Json.toJson(newAtom))
          }
        )
      }
    }
  }

  def now() = new Date().getTime

  def revertAtom(atomId: String, version: Long) = APIAuthAction { implicit req =>
    getAtom(atomId, previewDataStore) { atom =>
      if(!atom.tdata.assets.exists(_.version == version)) {
        InternalServerError(jsonError(s"no asset is listed for version $version"))
      } else {
        val newAtom = atom
          .withRevision(_ + 1)
          .updateData { media => media.copy(activeVersion = Some(version)) }

        previewDataStore.updateAtom(newAtom)
        Ok(Json.toJson(newAtom))
      }
    }
  }

  // TODO -> this needs to handle paging
  def listAtoms = APIAuthAction { implicit req =>
    previewDataStore.listAtoms().fold(
      err =>   InternalServerError(jsonError(err.msg)),
      atoms => {
        val hostedMediaAtoms = atoms
          .toList
          .filter(_.tdata.category == Hosted)

        Ok(Json.toJson(hostedMediaAtoms))
      }
    )
  }

  def getConfigValues = APIAuthAction { implicit req =>

    val stage = awsConfig.stage

    Ok(Json.toJson(Map("stage" -> stage)))

  }

  private def getAtom(atomId: String, store: DataStore)(fn: Atom => Result): Result = {
    store.getAtom(atomId) match {
      case Right(atom) => fn(atom)
      case Left(IDNotFound) => NotFound(s"atom not found $atomId")
      case Left(err) => InternalServerError(err.msg)
    }
  }
}
