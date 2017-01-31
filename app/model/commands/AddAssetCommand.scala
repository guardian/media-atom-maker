package model.commands

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.atom.media.Category.Hosted
import com.gu.contentatom.thrift.atom.media.{Asset, Platform}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.AuditDataStore
import model.MediaAtom
import model.commands.CommandExceptions._
import util.atom.MediaAtomImplicits
import util.{Logging, ThriftUtil, YouTubeConfig, YouTubeVideoInfoApi}

case class AddAssetCommand(atomId: String,
                           videoUri: String,
                           version: Option[Long],
                           mimeType: Option[String])
                          (implicit previewDataStore: PreviewDataStore,
                           previewPublisher: PreviewAtomPublisher,
                           val youtubeConfig: YouTubeConfig,
                           auditDataStore: AuditDataStore,
                           user: PandaUser)
    extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  private def validateYoutubeOwnership (asset: Asset) = {
    asset.platform match {
      case Platform.Youtube => {
        val isMine = YouTubeVideoInfoApi(youtubeConfig).isMyVideo(asset.id)

        if (! isMine) {
          log.info(s"Cannot add asset $videoUri to $atomId. The youtube video is not on a Guardian channel")
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
    log.info(s"Request to add new asset $videoUri to $atomId")

    previewDataStore.getAtom(atomId) match {
      case Some(atom) =>
        val mediaAtom = atom.tdata
        val currentAssets: Seq[Asset] = mediaAtom.assets

        val resolvedVersion = version.getOrElse(currentAssets.foldLeft(1L){(acc, asset) => if (asset.version >= acc) asset.version + 1 else acc})

        if (currentAssets.exists(asset => asset.version == resolvedVersion && asset.mimeType == mimeType)) {
          log.info(s"Cannot add asset $videoUri to $atomId. An asset already exists with version $resolvedVersion and mimeType $mimeType")
          AssetVersionConflict
        }

        val newAsset = ThriftUtil.parseAsset(videoUri, mimeType, resolvedVersion)
          .fold(err => AssetParseFailed, identity)

        if (mediaAtom.category != Hosted) {
          validateYoutubeOwnership(newAsset)
        }

        val assetDuration = getAssetDuration(newAsset)

        val updatedAtom = atom
          .withData(mediaAtom.copy(
            assets = newAsset +: currentAssets,
            duration = assetDuration
          ))

        log.info(s"Adding new asset $videoUri to $atomId")

        UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom)).process()

      case None =>
        log.info(s"Cannot add asset $videoUri to $atomId. No atom has that id")
        AtomNotFound
    }
  }
}
