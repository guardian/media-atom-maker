package model.commands

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.pandomainauth.model.{User => PandaUser}
import data.AuditDataStore
import model.commands.CommandExceptions._
import model.{MediaAtom, UpdatedMetadata}
import util.atom.MediaAtomImplicits
import util.{Logging, YouTubeConfig, YouTubeVideoInfoApi}

case class UpdateMetadataCommand(atomId: String,
                                 metadata: UpdatedMetadata)
                                (implicit previewDataStore: PreviewDataStore,
                                 previewPublisher: PreviewAtomPublisher,
                                 val youtubeConfig: YouTubeConfig,
                                 auditDataStore: AuditDataStore,
                                 user: PandaUser)
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

            UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom)).process()

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
