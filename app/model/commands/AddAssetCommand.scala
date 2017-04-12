package model.commands

import com.gu.contentatom.thrift.Atom
import com.gu.contentatom.thrift.atom.media.Category.Hosted
import com.gu.contentatom.thrift.atom.media.{Asset, Platform, MediaAtom => ThriftMediaAtom}
import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTube
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model._
import model.MediaAtom.fromThrift
import model.commands.CommandExceptions._
import util.ThriftUtil
import util.atom.MediaAtomImplicits

case class AddAssetCommand(atomId: String, videoUri: String, override val stores: DataStores,
                           youTube: YouTube, user: PandaUser)
    extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): (MediaAtom, AuditEvent) = {
    log.info(s"Request to add new asset $videoUri to $atomId")

    val atom = getPreviewAtom(atomId)

    val mediaAtom = atom.tdata
    val currentAssets: Seq[Asset] = mediaAtom.assets

    videoUri match {
      case YouTubeId(videoId) if assetAlreadyExists(videoId, currentAssets) =>
        log.info(s"Cannot add asset $videoUri to $atomId as it already exists.")
        AssetVersionConflict

      case YouTubeId(videoId) =>
        addAsset(atom, mediaAtom, currentAssets, videoId)

      case _ =>
        NotYoutubeAsset
    }
  }

  private def addAsset(atom: Atom, mediaAtom: ThriftMediaAtom, currentAssets: Seq[Asset], videoId: String) = {
    val version = getNextAssetVersionNumber(currentAssets)

    val newAsset = ThriftUtil.parseAsset(uri = videoUri, version = version, mimeType = None)
      .fold(err => AssetParseFailed, identity)

    if (mediaAtom.category != Hosted) {
      validateYoutubeOwnership(newAsset)
    }

    val assetDuration = youTube.getDuration(videoId)

    val updatedAtom = atom
      .withData(mediaAtom.copy(
        assets = newAsset +: currentAssets,
        duration = assetDuration
      ))

    log.info(s"Adding new asset $videoUri to $atomId")

    val (result, _) = UpdateAtomCommand(atomId, fromThrift(updatedAtom), stores, user).process()
    // TODO MRB: asset source
    val event = AuditEvent.addAsset(user, atomId, model.Asset.fromThrift(newAsset), "unknown")

    (result, event)
  }

  private def validateYoutubeOwnership (asset: Asset) = {
    asset.platform match {
      case Platform.Youtube => {
        val isMine = youTube.isMyVideo(asset.id)

        if (! isMine) {
          log.info(s"Cannot add asset $videoUri to $atomId. The youtube video is not on a Guardian channel")
          NotGuardianYoutubeVideo
        }
      }
      case _ => None
    }
  }

  private def getNextAssetVersionNumber (currentAssets: Seq[Asset]): Long = {
    currentAssets.foldLeft(1L){ (acc, asset) => {
      if (asset.version >= acc) asset.version + 1 else acc
    }}
  }

  private def assetAlreadyExists (videoId: String, currentAssets: Seq[Asset]): Boolean = {
    currentAssets.exists(x => x.platform == Platform.Youtube && x.id == videoId)
  }

  private case object YouTubeId {
    def unapply(videoUri: String): Option[String] = {
      val platform = ThriftUtil.parsePlatform(videoUri)

      (platform, videoUri) match {
        case (Right(Platform.Youtube), ThriftUtil.youtube(videoId)) =>
          Some(videoId)

        case _ =>
          None
      }
    }
  }
}
