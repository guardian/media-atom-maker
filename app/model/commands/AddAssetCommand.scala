package model.commands

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.atom.media.Category.Hosted
import com.gu.contentatom.thrift.atom.media.{Asset, Platform}
import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.AuditDataStore
import model.MediaAtom
import model.commands.CommandExceptions._
import util.atom.MediaAtomImplicits
import util.{ThriftUtil, YouTubeConfig, YouTubeVideoInfoApi}

case class AddAssetCommand(atomId: String, videoUri: String, previewDataStore: PreviewDataStore,
                           previewPublisher: PreviewAtomPublisher, youtubeConfig: YouTubeConfig,
                           auditDataStore: AuditDataStore, user: PandaUser)
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

  private def getNextAssetVersionNumber (currentAssets: Seq[Asset]): Long = {
    currentAssets.foldLeft(1L){ (acc, asset) => {
      if (asset.version >= acc) asset.version + 1 else acc
    }}
  }

  private def doesAssetAlreadyExist (videoId: String, currentAssets: Seq[Asset]): Boolean = {
    currentAssets.exists(x => x.platform == Platform.Youtube && x.id == videoId)
  }

  def process(): MediaAtom = {
    log.info(s"Request to add new asset $videoUri to $atomId")

    previewDataStore.getAtom(atomId) match {
      case Some(atom) => {
        ThriftUtil.parsePlatform(videoUri) match {
          case Right(Platform.Youtube) => {
            val mediaAtom = atom.tdata
            val currentAssets: Seq[Asset] = mediaAtom.assets

            videoUri match {
              case ThriftUtil.youtube(videoId) => {
                if (doesAssetAlreadyExist(videoId, currentAssets)) {
                  log.info(s"Cannot add asset $videoUri to $atomId as it already exists.")
                  AssetVersionConflict
                } else {
                  val version = getNextAssetVersionNumber(currentAssets)

                  val newAsset = ThriftUtil.parseAsset(uri = videoUri, version = version, mimeType = None)
                    .fold(err => AssetParseFailed, identity)

                  if (mediaAtom.category != Hosted) {
                    validateYoutubeOwnership(newAsset)
                  }

                  val assetDuration = YouTubeVideoInfoApi(youtubeConfig).getDuration(videoId)

                  val updatedAtom = atom
                    .withData(mediaAtom.copy(
                      assets = newAsset +: currentAssets,
                      duration = assetDuration
                    ))

                  log.info(s"Adding new asset $videoUri to $atomId")

                  UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom), previewDataStore, previewPublisher, auditDataStore, user).process()
              }
            }
            case _ => NotYoutubeAsset
          }
        }
        case _ => NotYoutubeAsset
      }
    }
    case None =>
      log.info(s"Cannot add asset $videoUri to $atomId. No atom has that id")
      AtomNotFound
    }
  }
}
