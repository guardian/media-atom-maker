package model.commands

import com.gu.contentatom.thrift.AtomData.Media
import com.gu.media.logging.Logging
import com.gu.media.util.{SelfHostedAsset, VideoAsset, YouTubeAsset}
import com.gu.media.youtube.YouTube
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.{Asset, MediaAtom}
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
    val withAssets = VideoAsset.addToAtom(atom.tdata, asset)

    val existingChannel = withAssets.metadata.flatMap(_.channelId)
    val channel = getYouTubeChannel(asset, existingChannel)

    val metadata = withAssets.metadata.map(_.copy(channelId = channel))
    val withChannel = withAssets.copy(metadata = metadata)

    val mediaAtom = MediaAtom.fromThrift(atom.copy(data = Media(withChannel)))

    checkNotAlreadyAdded(mediaAtom.assets, asset)

    log.info(s"Adding new asset $asset to $atomId")
    UpdateAtomCommand(atomId, mediaAtom, stores, user).process()
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
