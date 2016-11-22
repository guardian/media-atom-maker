package model.commands

import java.util.Date
import scala.util.{Failure, Success}
import CommandExceptions._

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{EventType, ContentAtomEvent}
import com.gu.contentatom.thrift.atom.media.Asset
import util.atom.MediaAtomImplicits
import util.{YouTubeConfig, YouTubeVideoStatusApi}


case class ActiveAssetCommand(atomId: String, youtubeId: String)
                             (implicit previewDataStore: PreviewDataStore,
                              previewPublisher: PreviewAtomPublisher,
                              val youtubeConfig: YouTubeConfig)
  extends Command
  with MediaAtomImplicits {

  type T = Unit

  def process(): Unit = {

    val videoStatus = YouTubeVideoStatusApi(youtubeConfig).get(youtubeId)

    videoStatus match {
      case Some("succeeded") => {

        previewDataStore.getAtom(atomId) match {
          case Some(atom) =>
            val mediaAtom = atom.tdata
            val atomAssets: Seq[Asset] = mediaAtom.assets

            val newActiveAsset = atomAssets.find(asset => asset.id.split("v=")(1) == youtubeId).get

            val newAtom = atom
              .withData(mediaAtom.copy(
                activeVersion = Some(newActiveAsset.version)
              ))
              .withRevision(_ + 1)

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
          case None => AtomNotFound
        }
      }
      case Some(_) => AssetEncodingInProcess
      case None => NotYoutubeAsset
    }
  }
}
