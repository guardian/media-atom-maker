package model.commands

import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTube
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.{Asset, MediaAtom}
import util.{AddAssetRequest, AddSelfHostedAsset, AddYouTubeAsset}
import util.atom.MediaAtomImplicits

case class AddAssetCommand(atomId: String, params: AddAssetRequest, override val stores: DataStores,
                           youTube: YouTube, user: PandaUser)
  extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): MediaAtom = {
    log.info(s"Request to $params on $atomId")

    val atom = getPreviewAtom(atomId)
    val mediaAtom = MediaAtom.fromThrift(atom)

    val currentAssets = mediaAtom.assets
    val nextVersion = getNextVersion(currentAssets)

    val (newAssets, youTubeChannel) = validate(params, mediaAtom.channelId)
    val versionedAssets = newAssets.map(_.copy(version = nextVersion))

    versionedAssets.foreach { asset =>
      checkNotAlreadyAdded(currentAssets, asset)
    }

    val updatedAtom = mediaAtom.copy(
      channelId = youTubeChannel,
      assets = versionedAssets ++ currentAssets
    )

    log.info(s"Adding new asset ${newAssets.mkString(",")} to $atomId")
    UpdateAtomCommand(atomId, updatedAtom, stores, user).process()
  }

  private def validate(params: AddAssetRequest, existingChannel: Option[String]): (List[Asset], Option[String]) = params match {
    case AddYouTubeAsset(asset) =>
      val channel = validateYouTubeChannel(asset.id, existingChannel)
      (List(asset), Some(channel))

    case AddSelfHostedAsset(assets) =>
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
