package model.commands

import com.gu.media.youtube.{YouTubeAccess, YouTubeVideos}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.{MediaAtom, UpdatedMetadata}

case class UpdateMetadataCommand(atomId: String, metadata: UpdatedMetadata, user: PandaUser,
                                 youTube: YouTubeVideos, stores: DataStores) extends Command {

  type T = Unit

  def process(): Unit = {
    log.info(s"Request to update metadata for $atomId")

    stores.preview.getAtom(atomId) match {
      case Some(atom) =>
        val thriftMediaAtom = atom.tdata
        val mediaAtom = MediaAtom.fromThrift(atom)

        MediaAtom.getActiveYouTubeAsset(mediaAtom) match {
          case Some(youtubeAsset) =>

            val newMetadata = thriftMediaAtom.metadata.map(_.copy(
              tags = metadata.tags,
              categoryId = metadata.categoryId,
              license = metadata.license,
              privacyStatus = metadata.privacyStatus.flatMap(_.asThrift),
              expiryDate = metadata.expiryDate
            ))

            val activeYTAssetDuration = youTube.getDuration(youtubeAsset.id)

            val updatedAtom = atom.updateData { media =>
                media.copy(
                  description = metadata.description,
                  metadata = newMetadata,
                  duration = activeYTAssetDuration,
                  plutoProjectId = metadata.plutoId
                )
            }

            UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom), user, stores).process()

          case None =>
            log.info(s"Unable to update metadata for $atomId. Atom does not have an active asset")

            NotYoutubeAsset
        }
      case None =>
        log.info(s"Unable to update metadata for $atomId. Atom does not exist")
        AtomNotFound
    }
  }
}
