package model.commands

import java.util.Date

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import model.{MediaAtom, UpdatedMetadata}
import model.commands.CommandExceptions._
import util.{YouTubeConfig, YouTubeVideoInfoApi, YouTubeVideoUpdateApi}
import util.atom.MediaAtomImplicits
import data.AuditDataStore

import scala.util.{Failure, Success}

import com.gu.pandomainauth.model.{User => PandaUser}

case class UpdateMetadataCommand(atomId: String,
                                 metadata: UpdatedMetadata)
                                (implicit previewDataStore: PreviewDataStore,
                                 previewPublisher: PreviewAtomPublisher,
                                 val youtubeConfig: YouTubeConfig,
                                 auditDataStore: AuditDataStore,
                                 user: PandaUser)
    extends Command
    with MediaAtomImplicits {

  type T = Unit

  def process(): Unit = {
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
                  title = metadata.title,
                  description = metadata.description,
                  metadata = newMetadata,
                  duration = activeYTAssetDuration,
                  plutoProjectId = metadata.plutoId
                )
            }

            UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom)).process()

          case None => NotYoutubeAsset
        }
      case None => AtomNotFound
    }
  }
}
