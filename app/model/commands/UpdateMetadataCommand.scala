package model.commands

import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTube
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.{Audit, MediaAtom, UpdatedMetadata}
import util.atom.MediaAtomImplicits

case class UpdateMetadataCommand(atomId: String, metadata: UpdatedMetadata, override val stores: DataStores,
                                 youTube: YouTube, user: PandaUser)
    extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): (MediaAtom, Audit) = {
    log.info(s"Request to update metadata for $atomId")

    val atom = getPreviewAtom(atomId)

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

        UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom), stores, user).process()

      case None =>
        log.info(s"Unable to update metadata for $atomId. Atom does not have an active asset")

        NotYoutubeAsset
    }
  }
}
