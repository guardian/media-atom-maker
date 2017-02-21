package model.commands

import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.{MediaAtom, UpdatedMetadata}
import util.atom.MediaAtomImplicits
import util.{YouTubeConfig, YouTubeVideoInfoApi}

case class UpdateMetadataCommand(atomId: String, metadata: UpdatedMetadata, override val stores: DataStores,
                                 youtubeConfig: YouTubeConfig, user: PandaUser)
    extends Command
    with MediaAtomImplicits
    with Logging {

  type T = Unit

  def process(): Unit = {
    log.info(s"Request to update metadata for $atomId")

    previewDataStore.getAtom(atomId) match {
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

            val activeYTAssetDuration = YouTubeVideoInfoApi(youtubeConfig).getDuration(youtubeAsset.id)

            val updatedAtom = atom.updateData { media =>
                media.copy(
                  description = metadata.description,
                  metadata = newMetadata,
                  duration = activeYTAssetDuration,
                  plutoProjectId = metadata.plutoId
                )
            }

            UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom), stores, user).process()

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
