package model.commands

import com.gu.media.logging.Logging
import com.gu.media.model.{Asset, Image, ImageAsset, MediaAtom, VideoAsset}
import com.gu.media.model.Platform.{Url, Youtube}
import com.gu.media.util.MediaAtomImplicits
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import util._

case class ActiveAssetCommand(
  atomId: String,
  activateAssetRequest: ActivateAssetRequest,
  stores: DataStores,
  youtube: YouTube,
  user: PandaUser,
  awsConfig: AWSConfig
) extends Command with MediaAtomImplicits with Logging {

  type T = MediaAtom

  def process(): T = {
    log.info(s"Request to set active asset atom=$atomId version=${activateAssetRequest.version}")

    if (atomId != activateAssetRequest.atomId) {
      AtomIdConflict
    }

    val atom = getPreviewAtom(atomId)
    val mediaAtom = MediaAtom.fromThrift(atom)

    val assetsToActivate = mediaAtom.assets.filter(_.version == activateAssetRequest.version)

    if (assetsToActivate.nonEmpty) {
      val duration = assetsToActivate.find(_.platform == Youtube)
        .flatMap(asset => youtube.getDuration(asset.id))
        .orElse(mediaAtom.duration)

      val posterImage = derivePosterImage(mediaAtom, activateAssetRequest.version)

      val updatedAtom = mediaAtom.copy(
        activeVersion = Some(activateAssetRequest.version),
        duration = duration,
        posterImage = posterImage
      )

      log.info(s"Setting active asset atom=$atomId version=${activateAssetRequest.version}")
      UpdateAtomCommand(atomId, updatedAtom, stores, user, awsConfig).process()
    } else {
      log.info(s"Cannot set active asset. No asset has that version atom=$atomId version=${activateAssetRequest.version}")
      AssetVersionConflict
    }
  }

  private [commands] def firstFrameImageName(mp4Name: String): String = {
    // drop .mp4 and replace with image suffix
    mp4Name.dropRight(4).concat(VideoAsset.firstFrameImageSuffix)
  }

  // could go in MediaAtomHelpers
  private [commands] def findSelfHostedAsset(mediaAtom: MediaAtom, mimeType: String, version: Long): Option[Asset] =
    mediaAtom.assets.find(asset =>
      asset.platform == Url &&
        asset.mimeType.contains(mimeType) &&
        asset.version == version
    )

  private [commands] def findActiveSelfHostedAsset(mediaAtom: MediaAtom, mimeType: String): Option[Asset] =
    mediaAtom.activeVersion.flatMap(ver => findSelfHostedAsset(mediaAtom, mimeType, ver))

  private [commands] def checkImageExists(fileName: String): Boolean = true

  /** auto-populate posterImage if:
   *  - the new asset version to activate is self-hosted
   *  AND
   *  - a first-frame image is available
   *  AND (
   *  - the current posterImage is empty
   *  OR
   *  - the current posterImage was auto-populated
   *        i.e. jpg matches mp4 asset id
   *  )
   **/
  def derivePosterImage(mediaAtom: MediaAtom, newVersion: Long): Option[Image] = {
    val selfHostMp4 = findSelfHostedAsset(mediaAtom, "video/mp4", newVersion)

    selfHostMp4 match {

      case Some(requestedMp4) =>
        val requestedFirstFrameImageName = firstFrameImageName(requestedMp4.id)
        val firstFrameImageAvailable = checkImageExists(requestedFirstFrameImageName)

        if (firstFrameImageAvailable) {
          // look at current state
          val currentMp4 = findActiveSelfHostedAsset(mediaAtom, "video/mp4")
          val currentPosterImageName = mediaAtom.posterImage.map(_.master)
          val currentFirstFrameImageName = currentMp4.map(mp4 => firstFrameImageName(mp4.id))
          val wasAutoPopulated = currentPosterImageName.isDefined && currentPosterImageName == currentFirstFrameImageName

          if (currentPosterImageName.isEmpty || wasAutoPopulated) {
            // TODO: what info has to go in the image?
            Image(
              assets = Nil,
              master = Some(
                ImageAsset(
                  mimeType = Some("image/jpeg"),
                  file = requestedFirstFrameImageName,
                  dimensions = None,
                  size = None,
                  aspectRatio = None)
              ),
              mediaId = mediaAtom.id,
              source = None
            )
          } else {
            mediaAtom.posterImage
          }
        }

      case None => mediaAtom.posterImage
    }
  }
}
