package model.commands

import com.gu.media.logging.Logging
import com.gu.media.model.{Image, MediaAtom, VideoSource}
import com.gu.media.model.Platform.Youtube
import com.gu.media.util.{MediaAtomHelpers, MediaAtomImplicits}
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
    imageUtil: S3ImageUtil
) extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  lazy val mediaConvertBucket: String =
    awsConfig.getMandatoryString("aws.mediaconvert.destinationBucket")

  def process(): T = {
    log.info(
      s"Request to set active asset atom=$atomId version=${activateAssetRequest.version}"
    )

    if (atomId != activateAssetRequest.atomId) {
      AtomIdConflict
    }

    val atom = getPreviewAtom(atomId)
    val mediaAtom = MediaAtom.fromThrift(atom)

    val assetsToActivate =
      mediaAtom.assets.filter(_.version == activateAssetRequest.version)

    if (assetsToActivate.nonEmpty) {
      val duration = assetsToActivate
        .find(_.platform == Youtube)
        .flatMap(asset => youtube.getDuration(asset.id))
        .orElse(mediaAtom.duration)

      // auto-populate posterImage with first frame of video if appropriate
      val posterImage =
        autoFirstFrameImage(mediaAtom, activateAssetRequest.version)
          .orElse(mediaAtom.posterImage)

      val updatedAtom = mediaAtom.copy(
        activeVersion = Some(activateAssetRequest.version),
        duration = duration,
        posterImage = posterImage
      )

      log.info(
        s"Setting active asset atom=$atomId version=${activateAssetRequest.version}"
      )
      UpdateAtomCommand(atomId, updatedAtom, stores, user, awsConfig).process()
    } else {
      log.info(
        s"Cannot set active asset. No asset has that version atom=$atomId version=${activateAssetRequest.version}"
      )
      AssetVersionConflict
    }
  }

  private[commands] def firstFrameImageName(mp4Name: String): String = {
    // drop .mp4 and replace with image suffix
    mp4Name.dropRight(4).concat(VideoSource.firstFrameImageSuffix)
  }

  /** use a default image generated from the first frame of video if:
    *   - the new asset version to activate is self-hosted AND
    *   - a first-frame image is available AND (
    *     - the current posterImage is empty OR
    *     - the current posterImage is using a default image
    *     )
    */
  def autoFirstFrameImage(
      mediaAtom: MediaAtom,
      newVersion: Long
  ): Option[Image] = {

    val currentPosterImage = mediaAtom.posterImage

    val currentFirstFrameImageName = MediaAtomHelpers
      .findActiveSelfHostedAsset(mediaAtom, "video/mp4")
      .map(currentMp4 => firstFrameImageName(currentMp4.id))

    val hasDefaultImage = currentFirstFrameImageName match {
      case Some(imageName) =>
        currentPosterImage.exists(img =>
          S3ImageUtil.imageHasUrl(img, imageName)
        )
      case _ => false
    }

    for {
      requestedMp4 <- MediaAtomHelpers.findSelfHostedAsset(
        mediaAtom,
        "video/mp4",
        newVersion
      )
      imgUrl = firstFrameImageName(requestedMp4.id)
      autoFirstFrameImage <- imageUtil.getS3Image(mediaConvertBucket, imgUrl)
      if currentPosterImage.isEmpty || hasDefaultImage
      _ = log.info(
        s"first frame image for version $newVersion of atom: $autoFirstFrameImage"
      )

    } yield autoFirstFrameImage
  }
}
