package model.commands

import java.net.URL
import java.time.Instant
import java.util.Date

import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import com.gu.media.youtube.{YouTube, YouTubeMetadataUpdate}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.Platform.Youtube
import model._
import model.commands.CommandExceptions._

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

case class PublishAtomCommand(id: String, fromExpiryPoller: Boolean, override val stores: DataStores, youTube: YouTube, user: PandaUser)
  extends Command with AtomAPIActions with Logging {

  type T = MediaAtom

  def process(): T = {
    log.info(s"Request to publish atom $id")

    val thriftAtom = getPreviewAtom(id)

    val atom = MediaAtom.fromThrift(thriftAtom)

    (atom.privacyStatus, !fromExpiryPoller) match {
      case (Some(PrivacyStatus.Private), false) =>
        log.error(s"Unable to publish atom ${atom.id}, privacy status is set to private")
        AtomPublishFailed("Atom status set to private")

      case _ => {

        MediaAtom.getActiveYouTubeAsset(atom) match {
          case Some(asset) => {
            try {
              updateThumbnail(atom)
            } catch {
              case NonFatal(e) => {
                log.error(s"Unable to update thumbnail for asset=${asset.id} atom={$id}", e)
                PosterImageUploadFailed(e.getMessage)
              }
            }
          }
          case None =>
            log.info(s"Unable to update thumbnail for $id. There is no active YouTube asset")
        }

        atom.activeVersion match {
          case Some(atomVersion) => {

            val activeAssetId = atom.assets.find(asset => {
              asset.version == atomVersion
            }).get.id

            val metadata = YouTubeMetadataUpdate(
              title = Some(atom.title),
              categoryId = atom.youtubeCategoryId,
              description = atom.description,
              tags = atom.tags,
              license = atom.license,
              privacyStatus = atom.privacyStatus.map(_.name)
            )

            youTube.updateMetadata(activeAssetId, metadata)
          }
          case None =>
            log.info(s"Not updating YouTube metadata for atom $id as it has no active asset")
        }

        val changeRecord = Some(ChangeRecord.now(user).asThrift)

        val updatedAtom = thriftAtom.copy(
          contentChangeDetails = thriftAtom.contentChangeDetails.copy(
            published = changeRecord,
            lastModified = changeRecord,
            revision = thriftAtom.contentChangeDetails.revision + 1
          )
        )

        log.info(s"Publishing atom $id")

        auditDataStore.auditPublish(id, user)
        UpdateAtomCommand(id, MediaAtom.fromThrift(updatedAtom), stores, user).process()

        setAssetsToPrivate(atom)
        publishAtomToLive(updatedAtom)
      }
    }
  }

  private def publishAtomToLive(atom: Atom): MediaAtom = {
    val event = ContentAtomEvent(atom, EventType.Update, (new Date()).getTime())

    livePublisher.publishAtomEvent(event) match {
      case Success(_) =>
        publishedDataStore.updateAtom(atom) match {
          case Right(_) => {
            log.info(s"Successfully published atom: ${id}")
            MediaAtom.fromThrift(atom)
          }
          case Left(err) =>
            log.error("Unable to update datastore after publish", err)
            AtomPublishFailed(s"Could not update published datastore after publish: ${err.toString}")
        }
      case Failure(err) =>
        log.error("Unable to publish atom to kinesis", err)
        AtomPublishFailed(s"Could not publish atom (live kinesis event failed): ${err.toString}")
    }
  }

  private def updateThumbnail(atom: MediaAtom): Unit = {
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

    youTube.updateThumbnail(asset.id, new URL(img.file), img.mimeType.get)
  }

  private def setAssetsToPrivate(atom: MediaAtom): Unit = {
    val nowMillis = Instant.now().toEpochMilli
    val expired = atom.expiryDate.exists(_ < nowMillis)

    MediaAtom.getActiveYouTubeAsset(atom).foreach { activeAsset =>
      val youTubeAssets = atom.assets.filter(_.platform == Youtube)
      val toMakePrivate = if(expired) { youTubeAssets } else { youTubeAssets.filterNot(_.id == activeAsset.id) }

      toMakePrivate.foreach { asset =>
        log.info(s"Marking asset=${asset.id} atom=${atom.id} as private")
        youTube.setStatusToPrivate(asset.id)
      }
    }
  }
}
