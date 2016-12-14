package model.commands

import java.util.Date

import com.gu.atom.data.PreviewDataStore
import CommandExceptions._
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.atom.media.Category.Hosted
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.contentatom.thrift.atom.media.{Asset, Platform}
import model.MediaAtom
import util.{ThriftUtil, YouTubeConfig, YouTubeVideoInfoApi}
import util.atom.MediaAtomImplicits

import scala.util.{Failure, Success}

case class AddAssetCommand(atomId: String,
                           videoUri: String,
                           version: Option[Long],
                           mimeType: Option[String])
                          (implicit previewDataStore: PreviewDataStore,
                           previewPublisher: PreviewAtomPublisher,
                           val youtubeConfig: YouTubeConfig)
    extends Command
    with MediaAtomImplicits {

  type T = MediaAtom

  private def validateYoutubeOwnership (asset: Asset) = {
    asset.platform match {
      case Platform.Youtube => {
        val isMine = YouTubeVideoInfoApi(youtubeConfig).isMyVideo(asset.id)

        if (! isMine) {
          NotGuardianYoutubeVideo
        }
      }
      case _ => None
    }
  }

  private def getAssetDuration (asset: Asset): Option[Long] = {
    asset.platform match {
      case Platform.Youtube => YouTubeVideoInfoApi(youtubeConfig).getDuration(asset.id)
      case _ => None
    }
  }

  def process(): MediaAtom = {
    previewDataStore.getAtom(atomId) match {
      case Some(atom) =>
        val mediaAtom = atom.tdata
        val currentAssets: Seq[Asset] = mediaAtom.assets

        val resolvedVersion = version.getOrElse(currentAssets.foldLeft(1L){(acc, asset) => if (asset.version >= acc) asset.version + 1 else acc})

        if (currentAssets.exists(asset => asset.version == resolvedVersion && asset.mimeType == mimeType)) {
          AssetVersionConflict
        }

        val newAsset = ThriftUtil.parseAsset(videoUri, mimeType, resolvedVersion)
          .fold(err => AssetParseFailed, identity)

        if (mediaAtom.category != Hosted) {
          validateYoutubeOwnership(newAsset)
        }

        val assetDuration = getAssetDuration(newAsset)

        val newAtom = atom
          .withData(mediaAtom.copy(
            assets = newAsset +: currentAssets,
            duration = assetDuration
          ))
          .withRevision(_ + 1)

        previewDataStore.updateAtom(newAtom).fold(
          err => AtomUpdateFailed(err.msg),
          _ => {
            val event = ContentAtomEvent(newAtom, EventType.Update, new Date().getTime)

            previewPublisher.publishAtomEvent(event) match {
              case Success(_) => return MediaAtom.fromThrift(newAtom)
              case Failure(err) => AtomPublishFailed(err.toString)
            }
          }
        )
      case None => AtomNotFound
    }
  }
}
