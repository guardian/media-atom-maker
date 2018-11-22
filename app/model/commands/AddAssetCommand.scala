package model.commands

import com.gu.contentatom.thrift.Atom
import com.gu.contentatom.thrift.atom.media.{Asset, Metadata, Platform, Category => ThriftCategory, MediaAtom => ThriftMediaAtom}
import com.gu.media.logging.Logging
import com.gu.media.model.MediaAtom
import com.gu.media.util.{MediaAtomImplicits, ThriftUtil}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import com.gu.media.model.MediaAtom.fromThrift
import model.commands.CommandExceptions._
import util.{AWSConfig, YouTube}

case class AddAssetCommand(atomId: String, videoUri: String, override val stores: DataStores,
                           youTube: YouTube, user: PandaUser, awsConfig: AWSConfig)
    extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): MediaAtom = {
    log.info(s"Request to add new asset $videoUri to $atomId")

    val atom = getPreviewAtom(atomId)

    val mediaAtom = atom.tdata
    val currentAssets: Seq[Asset] = mediaAtom.assets

    videoUri match {
      case YouTubeId(videoId) if assetAlreadyExists(videoId, currentAssets) =>
        log.info(s"Cannot add asset $videoUri to $atomId as it already exists.")
        AssetAlreadyAdded

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

    val channel = getYouTubeChannel(newAsset, mediaAtom)
    val metadata = mediaAtom.metadata.getOrElse(Metadata()).copy(channelId = channel)

    val updatedAtom = atom
      .withData(mediaAtom.copy(
        assets = newAsset +: currentAssets,
        metadata = Some(metadata)
      ))

    log.info(s"Adding new asset $videoUri to $atomId")

    UpdateAtomCommand(atomId, fromThrift(updatedAtom), stores, user, awsConfig).process()
  }

  private def getYouTubeChannel(asset: Asset, atom: ThriftMediaAtom): Option[String] = {
    val maybeChannel = atom.metadata.flatMap(_.channelId)
    val maybeVideo = youTube.getVideo(asset.id, "snippet")


    (maybeChannel, maybeVideo) match {
      case (_, None) => Some(YouTubeVideoDoesNotExist(asset.id))

      case (None, Some(video)) => {
        // only GLabs atoms can have third party videos
        atom.category match {
          case ThriftCategory.Hosted | ThriftCategory.Paid => Some(video.getSnippet.getChannelId)
          case _ => {
            if (youTube.cannotReachYoutube) None
            else NotGLabsAtom
          }
        }
      }

      case (Some(channel), Some(video)) => {
        val videoChannel = video.getSnippet.getChannelId

        if (channel != videoChannel) {
          log.info(s"Atom channel updating. New asset ${asset.id} on channel $videoChannel, atom on channel $channel")
        }

        // new asset must be on a Guardian channel
        if (!youTube.channels.exists(_.id == videoChannel)) IncorrectYouTubeChannel else Some(videoChannel)
      }
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
