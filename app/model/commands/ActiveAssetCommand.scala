package model.commands

import com.gu.media.logging.Logging
import com.gu.media.model.{Asset, Image, MediaAtom, VideoAsset}
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
  awsConfig: AWSConfig,
  imageUtil: ImageUtil
) extends Command with MediaAtomImplicits with Logging {

  type T = MediaAtom

  lazy val mediaConvertBucket: String = awsConfig.getMandatoryString("aws.mediaconvert.destinationBucket")

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

      // auto-populate posterImage with first frame of video if appropriate
      val posterImage = autoFirstFrameImage(mediaAtom, activateAssetRequest.version)
        .orElse(mediaAtom.posterImage)

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

  private [commands] def firstFrameImage(mediaId: String, mp4Name: String): Option[Image] = {
    val imageUrl = """(https://.*?)/(.*)""".r

    firstFrameImageName(mp4Name) match {

      case imageUrl(httpOrigin, s3Key) =>
        val s3ImageAsset = imageUtil.getS3ImageAsset(mediaConvertBucket, s3Key, httpOrigin)
        s3ImageAsset.map { asset =>
          Image(
            assets = List(asset),
            master = Some(asset),
            mediaId = mediaId,
            source = None
          )
        }

      case _ => None
    }
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

  /**
   * use a default image generated from the first frame of video if:
   *  - the new asset version to activate is self-hosted
   *  AND
   *  - a first-frame image is available
   *  AND (
   *    - the current posterImage is empty
   *    OR
   *    - the current posterImage is using a default image
   *  )
   */
  def autoFirstFrameImage(mediaAtom: MediaAtom, newVersion: Long): Option[Image] = {

    log.info(s"first frame image for version $newVersion of $mediaAtom")

    val currentPosterImage = mediaAtom.posterImage
    log.info(s"current posterImage $currentPosterImage")

    val currentFirstFrameImageName = findActiveSelfHostedAsset(mediaAtom, "video/mp4")
      .map(currentMp4 => firstFrameImageName(currentMp4.id))
    log.info(s"currentFirstFrameImageName $currentFirstFrameImageName")

    val hasDefaultImage = currentFirstFrameImageName match {
      case Some(imageName) =>
        currentPosterImage.exists(img => ImageUtil.imageHasUrl(img, imageName))
      case _ => false
    }
    log.info(s"hasDefaultImage $hasDefaultImage")

    for {
      requestedMp4 <- findSelfHostedAsset(mediaAtom, "video/mp4", newVersion)
      _ = log.info(s"requestedMp4 $requestedMp4")
      autoFirstFrameImage <- firstFrameImage(mediaAtom.id, requestedMp4.id)
        if currentPosterImage.isEmpty || hasDefaultImage
      _ = log.info(s"autoFirstFrameImage $autoFirstFrameImage")
    } yield autoFirstFrameImage
  }
}
