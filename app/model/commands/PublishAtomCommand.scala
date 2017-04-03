package model.commands

import java.net.URL
import java.time.Instant
import java.util.Date

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import com.gu.media.youtube.{YouTube, YouTubeMetadataUpdate}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.Platform.Youtube
import model._
import model.commands.CommandExceptions._

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

case class PublishAtomCommand(id: String, override val stores: DataStores, youTube: YouTube, user: PandaUser)
  extends Command with AtomAPIActions with Logging {

  type T = MediaAtom

  def process(): T = {
    log.info(s"Request to publish atom $id")

    val thriftAtom = getPreviewAtom(id)
    val atom = MediaAtom.fromThrift(thriftAtom)

    if(atom.privacyStatus.contains(PrivacyStatus.Private)) {
      log.error(s"Unable to publish atom ${atom.id}, privacy status is set to private")
      AtomPublishFailed("Atom status set to private")
    }

    getActiveAsset(atom) match {
      case Some(asset) if asset.platform == Youtube =>
        val atomWithDuration = atom.copy(duration = youTube.getDuration(asset.id))

        updateThumbnail(atomWithDuration, asset)
        updateYouTube(atomWithDuration, asset)
        publish(atomWithDuration, user)

      case _ =>
        publish(atom, user)
    }
  }

  private def publish(atom: MediaAtom, user: PandaUser): MediaAtom = {
    log.info(s"Publishing atom $id")

    val changeRecord = Some(ChangeRecord.now(user))

    val updatedAtom = atom.copy(
      contentChangeDetails = atom.contentChangeDetails.copy(
        published = changeRecord,
        lastModified = changeRecord,
        revision = atom.contentChangeDetails.revision + 1
      )
    )

    auditDataStore.auditPublish(id, user)
    UpdateAtomCommand(id, updatedAtom, stores, user).process()

    val publishedAtom = publishAtomToLive(updatedAtom)
    setAssetsToPrivate(publishedAtom)

    publishedAtom
  }

  private def publishAtomToLive(mediaAtom: MediaAtom): MediaAtom = {
    val atom = mediaAtom.asThrift
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

  private def updateYouTube(atom: MediaAtom, asset: Asset): Unit = {
    val metadata = YouTubeMetadataUpdate(
      title = Some(atom.title),
      categoryId = atom.youtubeCategoryId,
      description = atom.description,
      tags = atom.tags,
      license = atom.license,
      privacyStatus = atom.privacyStatus.map(_.name)
    )

    youTube.updateMetadata(asset.id, metadata)
  }

  private def updateThumbnail(atom: MediaAtom, asset: Asset): Unit = {
    try {
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
    } catch {
      case e: GoogleJsonResponseException if e.getDetails.getCode == 503 => { YouTubeConnectionIssue }
      case NonFatal(e) =>
        log.error(s"Unable to update thumbnail for asset=${asset.id} atom={$id}", e)
        PosterImageUploadFailed(e.getMessage)
    }
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

  private def getActiveAsset(atom: MediaAtom): Option[Asset] = for {
    version <- atom.activeVersion
    asset <- atom.assets.find(_.version == version)
  } yield asset
}
