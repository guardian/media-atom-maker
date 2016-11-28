package controllers

import java.util.Date
import javax.inject._

import util.{ThriftUtil, AWSConfig}
import com.gu.atom.data._
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.pandahmac.HMACAuthActions
import data.JsonConversions._
import ThriftUtil._
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.atom.MediaAtomImplicits
import play.api.libs.json._

import com.gu.atom.play._

import scala.util.{Failure, Success}

class Api @Inject() (val previewDataStore: PreviewDataStore,
                     val publishedDataStore: PublishedDataStore,
                     val livePublisher: LiveAtomPublisher,
                     val previewPublisher: PreviewAtomPublisher,
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
    previewDataStore.getAtom(id) match {
      case Some(atom) => Ok(Json.toJson(atom))
      case None => NotFound(jsonError(s"no atom with id $id found"))
    }
  }

  def getPublishedMediaAtom(id: String) = APIAuthAction { implicit req =>
    publishedDataStore.getAtom(id) match {
      case Some(atom) => Ok(Json.toJson(atom))
      case None => Ok("not published")
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
    previewDataStore.getAtom(atomId) match {
      case Some(atom) =>
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
      case None => NotFound(s"atom not found $atomId")
    }
  }

  def addAsset(atomId: String) = thriftResultAction(assetBodyParser) { implicit req =>
    val newAsset = req.body

    previewDataStore.getAtom(atomId) match {
      case Some(atom) =>
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
      case None => NotFound(s"atom not found $atomId")
    }
  }

  def now() = new Date().getTime

  def revertAtom(atomId: String, version: Long) = APIAuthAction { implicit req =>
    previewDataStore.getAtom(atomId) match {
      case Some(atom) =>
        if(!atom.tdata.assets.exists(_.version == version)) {
          InternalServerError(jsonError(s"no asset is listed for version $version"))
        } else {
          val newAtom = atom
            .withRevision(_ + 1)
            .updateData { media => media.copy(activeVersion = Some(version)) }

          previewDataStore.updateAtom(newAtom)
          Ok(Json.toJson(newAtom))
        }
      case None => NotFound(s"atom not found $atomId")
    }
  }

  // TODO -> this needs to handle paging
  def listAtoms = APIAuthAction { implicit req =>
    previewDataStore.listAtoms.fold(
      err =>   InternalServerError(jsonError(err.msg)),
      atoms => Ok(Json.toJson(atoms.toList))
    )
  }

  def getConfigValues = APIAuthAction { implicit req =>

    val stage = awsConfig.stage

    Ok(Json.toJson(Map("stage" -> stage)))

  }
}
