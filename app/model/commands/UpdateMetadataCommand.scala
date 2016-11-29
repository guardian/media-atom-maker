package model.commands

import java.util.Date

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import model.{MediaAtom, UpdatedMetadata}
import model.commands.CommandExceptions._
import util.{YouTubeConfig, YouTubeVideoInfoApi, YouTubeVideoUpdateApi}
import util.atom.MediaAtomImplicits

import scala.util.{Failure, Success}

case class UpdateMetadataCommand(atomId: String,
                                 metadata: UpdatedMetadata)
                                (implicit previewDataStore: PreviewDataStore,
                                 previewPublisher: PreviewAtomPublisher,
                                 val youtubeConfig: YouTubeConfig)
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
            YouTubeVideoUpdateApi(youtubeConfig).updateMetadata(youtubeAsset.id, metadata)

            val newMetadata = thriftMediaAtom.metadata.map(_.copy(
              tags = metadata.tags,
              categoryId = metadata.categoryId,
              license = metadata.license))

            val activeYTAssetDuration = YouTubeVideoInfoApi(youtubeConfig).getDuration(youtubeAsset.id)

            val newAtom = atom
              .withRevision(_ + 1)
              .updateData { media =>
                media.copy(description = metadata.description, metadata = newMetadata, duration = activeYTAssetDuration)}

            previewDataStore.updateAtom(newAtom).fold(
              err => InternalServerError(err.msg),
              _ => {
                val event = ContentAtomEvent(newAtom, EventType.Update, new Date().getTime)

                previewPublisher.publishAtomEvent(event) match {
                  case Success(_) => ()
                  case Failure(err) => InternalServerError(s"could not publish: ${err.toString}")
                }
              }
            )
          case None => NotYoutubeAsset
        }
      case None => AtomNotFound
    }
  }
}
