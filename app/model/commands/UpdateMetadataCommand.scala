package model.commands

import java.util.Date

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{EventType, ContentAtomEvent}
import com.gu.contentatom.thrift.atom.media.Platform
import model.UpdatedMetadata
import model.commands.CommandExceptions._
import util.{YouTubeConfig, YouTubeVideoUpdateApi}
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
        val mediaAtom = atom.tdata
        val assets = mediaAtom.assets
        val activeAsset = mediaAtom.activeVersion.flatMap(activeVersion => assets.find(_.version == activeVersion))

        activeAsset match {
          case Some(asset) =>

            if(asset.platform == Platform.Youtube)
              YouTubeVideoUpdateApi(youtubeConfig).updateMetadata(asset.id, metadata)

            val newMetadata = mediaAtom.metadata.map(_.copy(
              tags = metadata.tags,
              categoryId = metadata.categoryId,
              license = metadata.license))

            val newAtom = atom
              .withRevision(_ + 1)
              .updateData { media =>
                media.copy(description = metadata.description, metadata = newMetadata)}

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
          case None => AssetNotFound
        }
      case None => AtomNotFound
    }
  }
}
