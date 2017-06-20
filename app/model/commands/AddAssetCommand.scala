package model.commands

import com.gu.contentatom.thrift.atom.media.Asset
import com.gu.media.logging.Logging
import com.gu.media.util.{MediaAtomHelpers, SelfHostedAsset, VideoAsset, YouTubeAsset}
import com.gu.media.youtube.YouTube
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.MediaAtom
import model.commands.CommandExceptions._
import util.atom.MediaAtomImplicits
import MediaAtomHelpers._

case class AddAssetCommand(atomId: String, asset: VideoAsset, override val stores: DataStores,
                           youTube: YouTube, user: PandaUser)
  extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): MediaAtom = {
    log.info(s"Request to add $asset on $atomId")

    val before = getPreviewAtom(atomId)
    val after = updateAtom(before) { mediaAtom =>
      checkNotAlreadyAdded(mediaAtom.assets, asset)

      val existingChannel = mediaAtom.metadata.flatMap(_.channelId)
      val channel = getYouTubeChannel(asset, existingChannel)

      mediaAtom.copy(metadata = mediaAtom.metadata.map(_.copy(channelId = channel)))
    }

    log.info(s"Adding new asset $asset to $atomId")
    UpdateAtomCommand(atomId, MediaAtom.fromThrift(after), stores, user).process()
  }

  private def getYouTubeChannel(asset: VideoAsset, existingChannel: Option[String]): Option[String] = asset match {
    case YouTubeAsset(id) =>
      youTube.getVideo(id, "snippet") match {
        case Some(video) =>
          val channel = video.getSnippet.getChannelId

          existingChannel match {
            case Some(c) if c != channel =>
              YouTubeVideoOnIncorrectChannel(channel, c)

            case _ =>
              Some(channel)
          }

        case None =>
          YouTubeVideoDoesNotExist(id)
      }

    case _ =>
      existingChannel
  }

  private def checkNotAlreadyAdded(current: Seq[Asset], asset: VideoAsset) = {
    val ids = asset match {
      case YouTubeAsset(id) => List(id)
      case SelfHostedAsset(sources) => sources.map(_.src)
    }

    val inUse = ids.exists { id => current.exists(_.id == id) }

    if(inUse) {
      log.info(s"$asset has already been added to $atomId")
      AssetVersionConflict
    }
  }
}
