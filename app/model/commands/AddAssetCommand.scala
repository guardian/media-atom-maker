package model.commands

import com.gu.media.logging.Logging
import com.gu.media.util.{SelfHostedAsset, VideoAsset, VideoSource, YouTubeAsset}
import com.gu.media.youtube.YouTube
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.{Asset, AssetType, MediaAtom, Platform}
import util.atom.MediaAtomImplicits

case class AddAssetCommand(atomId: String, asset: VideoAsset, override val stores: DataStores,
                           youTube: YouTube, user: PandaUser)
  extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): MediaAtom = {
    log.info(s"Request to add $asset on $atomId")

    val atom = getPreviewAtom(atomId)
    val mediaAtom = MediaAtom.fromThrift(atom)

    val currentAssets = mediaAtom.assets
    val nextVersion = getNextVersion(currentAssets)

    val (newAssets, youTubeChannel) = validate(asset, nextVersion, mediaAtom.channelId)

    newAssets.foreach { asset =>
      checkNotAlreadyAdded(currentAssets, asset)
    }

    val updatedAtom = mediaAtom.copy(
      channelId = youTubeChannel,
      assets = newAssets ++ currentAssets
    )

    log.info(s"Adding new asset ${newAssets.mkString(",")} to $atomId")
    UpdateAtomCommand(atomId, updatedAtom, stores, user).process()
  }

  private def validate(asset: VideoAsset, version: Long, existingChannel: Option[String]): (List[Asset], Option[String]) = asset match {
    case YouTubeAsset(id) =>
      val asset = Asset(AssetType.Video, version, id, Platform.Youtube, mimeType = None)
      val channel = validateYouTubeChannel(asset.id, existingChannel)

      (List(asset), Some(channel))

    case SelfHostedAsset(sources) =>
      val assets = sources.map { case VideoSource(src, mimeType) =>
        Asset(AssetType.Video, version, src, Platform.Url, Some(mimeType))
      }

      (assets, existingChannel)
  }

  private def validateYouTubeChannel(id: String, existingChannel: Option[String]): String = {
    youTube.getVideo(id, "snippet") match {
      case Some(video) =>
        val newChannel = video.getSnippet.getChannelId

        existingChannel match {
          case Some(channel) if channel != newChannel =>
            YouTubeVideoOnIncorrectChannel(channel, newChannel)

          case _ =>
            newChannel
        }

      case None =>
        YouTubeVideoDoesNotExist(id)
    }
  }

  private def checkNotAlreadyAdded(current: Seq[Asset], asset: Asset) = {
    current.find(_.id == asset.id).foreach { _ =>
      log.info(s"${asset.id} has already been added to $atomId")
      AssetVersionConflict
    }
  }

  private def getNextVersion(assets: Seq[Asset]): Long = {
    if(assets.isEmpty) {
      1
    } else {
      assets.map(_.version).max + 1
    }
  }
}
