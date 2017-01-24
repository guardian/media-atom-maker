package model.commands

import java.net.URL
import java.util.Date

import play.api.Logger

import cats.data.Xor
import com.gu.atom.data.{PreviewDataStore, PublishedDataStore}
import com.gu.atom.play.AtomAPIActions
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.contentatom.thrift.{ChangeRecord, ContentAtomEvent, EventType, Atom}
import model.{ImageAsset, MediaAtom, UpdatedMetadata}
import util.{YouTubeConfig, YouTubeVideoUpdateApi}
import model.commands.CommandExceptions._
import data.AuditDataStore
import model._

import scala.util.{Failure, Success}
import scala.util.control.NonFatal

import com.gu.pandomainauth.model.{User => PandaUser}

case class PublishAtomCommand(id: String)(implicit val previewDataStore: PreviewDataStore,
                                          val previewPublisher: PreviewAtomPublisher,
                                          val publishedDataStore: PublishedDataStore,
                                          val livePublisher: LiveAtomPublisher,
                                          auditDataStore: AuditDataStore,
                                          youtubeConfig: YouTubeConfig,
                                          user: PandaUser) extends Command with AtomAPIActions {
  type T = MediaAtom

  def process(): T = {
    previewDataStore.getAtom(id) match {
      case None => AtomNotFound
      case Some(thriftAtom) => {
        val atom = MediaAtom.fromThrift(thriftAtom)
        val api = YouTubeVideoUpdateApi(youtubeConfig)

        MediaAtom.getActiveYouTubeAsset(atom) match {
          case Some(_) => {
            try {
              updateThumbnail(atom, api)
            } catch {
              case NonFatal(e) => {
                Logger.error(s"Active YouTube asset found but could not be updated for atom: ${id}")
                PosterImageUploadFailed(e.getMessage)
              }
            }
          }
          case None => Logger.info(s"No active YouTube asset found for atom: ${id}")
        }

        api.updateMetadata(id, UpdatedMetadata(
          atom.description,
          Some(atom.tags),
          atom.youtubeCategoryId,
          atom.license,
          atom.privacyStatus,
          atom.expiryDate,
          atom.plutoProjectId))

        val changeRecord = ChangeRecord((new Date()).getTime(), None) // Todo: User...
        val changeRecordAsModel = model.ChangeRecord.fromThrift(changeRecord)

        val updatedAtom = thriftAtom.copy(
          contentChangeDetails = thriftAtom.contentChangeDetails.copy(
            published = Some(changeRecord),
            lastModified = Some(changeRecord),
            revision = thriftAtom.contentChangeDetails.revision + 1
          )
        )

        auditDataStore.auditPublish(id, user)
        UpdateAtomCommand(id, MediaAtom.fromThrift(updatedAtom), Some(changeRecordAsModel)).process()

        publishAtomToLive(updatedAtom)

      }
    }
  }

  private def publishAtomToLive(atom: Atom): MediaAtom = {
    val event = ContentAtomEvent(atom, EventType.Update, (new Date()).getTime())

    livePublisher.publishAtomEvent(event) match {
      case Success(_) =>
        publishedDataStore.updateAtom(atom) match {
          case Xor.Right(_) => {
            Logger.info(s"Successfully published atom: ${id}")
            MediaAtom.fromThrift(atom)
          }
          case Xor.Left(err) => AtomPublishFailed(s"Could not update published datastore after publish: ${err.toString}")
        }
      case Failure(err) => AtomPublishFailed(s"Could not publish atom (live kinesis event failed): ${err.toString}")
    }
  }

  private def updateThumbnail(atom: MediaAtom, api: YouTubeVideoUpdateApi): Unit = {
    val asset = atom.getActiveAsset.get

    val master = atom.posterImage.flatMap(_.master).get
    val MAX_SIZE = 2000000
    val img: ImageAsset = if (master.size.get < MAX_SIZE) {
      master
    } else {
      // Get the biggest crop which is still less than MAX_SIZE
      atom.posterImage.map(
        _.assets
          .filter(a => a.size.nonEmpty && a.size.get < MAX_SIZE)
          .sortBy(_.size.get)
          .last
      ).get
    }

    api.updateThumbnail(asset.id, new URL(img.file), img.mimeType.get)

    Logger.info(s"Updated YouTube thumbnail for atom: ${id}")
  }
}
