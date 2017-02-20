package model.commands

import java.net.URL
import java.util.Date

import cats.data.Xor
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import com.gu.media.youtube.{YouTubeMetadataUpdate, YouTubeVideos}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.Platform.Youtube
import model._
import model.commands.CommandExceptions._

import scala.util.control.NonFatal
import scala.util.{Failure, Success}

case class PublishAtomCommand(id: String, user: PandaUser, stores: DataStores, youTube: YouTubeVideos) extends Command {
  type T = MediaAtom

  def process(): T = {
    log.info(s"Request to publish atom $id")

    stores.preview.getAtom(id) match {
      case None =>
        log.info(s"Unable to publish atom $id. No atom has that id")
        AtomNotFound

      case Some(thriftAtom) => {
        val atom = MediaAtom.fromThrift(thriftAtom)

        atom.privacyStatus match {
          case Some(PrivacyStatus.Private) =>
            log.error(s"Unable to publish atom ${atom.id}, privacy status is set to private")
            AtomPublishFailed("Atom status set to private")

          case _ => {

            MediaAtom.getActiveYouTubeAsset(atom) match {
              case Some(asset) => {
                try {
                  updateThumbnail(atom, youTube)
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

            stores.audit.auditPublish(id, user)
            UpdateAtomCommand(id, MediaAtom.fromThrift(updatedAtom), user, stores).process()

            setOldAssetsToPrivate(atom, youTube)
            publishAtomToLive(updatedAtom)
          }
        }
      }
    }
  }

  private def publishAtomToLive(atom: Atom): MediaAtom = {
    val event = ContentAtomEvent(atom, EventType.Update, (new Date()).getTime())

    stores.livePublisher.publishAtomEvent(event) match {
      case Success(_) =>
        stores.published.updateAtom(atom) match {
          case Xor.Right(_) => {
            log.info(s"Successfully published atom: ${id}")
            MediaAtom.fromThrift(atom)
          }
          case Xor.Left(err) =>
            log.error("Unable to update datastore after publish", err)
            AtomPublishFailed(s"Could not update published datastore after publish: ${err.toString}")
        }
      case Failure(err) =>
        log.error("Unable to publish atom to kinesis", err)
        AtomPublishFailed(s"Could not publish atom (live kinesis event failed): ${err.toString}")
    }
  }

  private def updateThumbnail(atom: MediaAtom, api: YouTubeVideos): Unit = {
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
  }

  private def setOldAssetsToPrivate(atom: MediaAtom, api: YouTubeVideos): Unit = {
    MediaAtom.getActiveYouTubeAsset(atom).foreach { activeAsset =>
      atom.assets.collect {
        case asset if asset.platform == Youtube && asset.id != activeAsset.id =>
          log.info(s"Marking asset=${asset.id} atom=${atom.id} as private")
          api.setStatusToPrivate(asset.id, atom.id)
      }
    }
  }
}
