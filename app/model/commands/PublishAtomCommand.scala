package model.commands

import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import com.gu.media.model.Platform.Youtube
import com.gu.media.model.{AuditMessage, _}
import com.gu.media.youtube.{YouTubeMetadataUpdate, YoutubeDescription}
import com.gu.media.{Capi, MediaAtomMakerPermissionsProvider}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model._
import model.commands.CommandExceptions._
import util.{AWSConfig, ThumbnailGenerator, YouTube}

import java.time.Instant
import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class PublishAtomCommand(
    id: String,
    override val stores: DataStores,
    youtube: YouTube,
    user: PandaUser,
    capi: Capi,
    permissionsProvider: MediaAtomMakerPermissionsProvider,
    awsConfig: AWSConfig,
    thumbnailGenerator: ThumbnailGenerator
) extends Command
    with Logging {

  type T = Future[MediaAtom]

  def process(): T = {
    log.info(s"Request to publish atom $id")

    val thriftPreviewAtom = getPreviewAtom(id)
    val previewAtom = MediaAtom.fromThrift(thriftPreviewAtom)

    if (previewAtom.privacyStatus.contains(PrivacyStatus.Private)) {
      log.error(
        s"Unable to publish atom ${previewAtom.id}, privacy status is set to private"
      )
      AtomPublishFailed("Atom status set to private")
    }

    val contentChangeDetails = thriftPreviewAtom.contentChangeDetails
    val now = Instant.now().toEpochMilli

    (
      contentChangeDetails.expiry,
      contentChangeDetails.scheduledLaunch,
      contentChangeDetails.embargo
    ) match {
      case (Some(expiry), _, _) if expiry.date <= now => {
        log.error(
          s"Unable to publish expired atom. atom=${previewAtom.id} expiry=${expiry.date}"
        )
        AtomPublishFailed("Atom has expired")
      }
      case (_, _, Some(embargo)) if embargo.date > now => {
        log.error(
          s"Unable to publish atom with embargo date. atom=${previewAtom.id} embargo=${embargo.date}"
        )
        AtomPublishFailed("Atom embargoed")
      }
      case (_, Some(schedule), _) if schedule.date > now => {
        log.error(
          s"Unable to publish atom as schedule time in the future. atom=${previewAtom.id} schedule=${schedule.date} now=$now"
        )
        AtomPublishFailed("Atom scheduled for the future")
      }
      case (_, Some(schedule), Some(embargo))
          if schedule.date < embargo.date => {
        log.error(
          s"Unable to publish atom as embargoed after schedule. atom=${previewAtom.id} schedule=${schedule.date} embargo=${embargo.date}"
        )
        AtomPublishFailed("Embargo set after schedule")
      }
      case (_, _, _) => {
        previewAtom.getActiveYouTubeAsset() match {
          case Some(asset) =>
            val publishedAtom = getPublishedAtom()

            val adSettings = AdSettings(
              youtube.minDurationForAds,
              youtube.minDurationForMidroll,
              previewAtom
            )
            val status = getResultingPrivacyStatus(previewAtom, publishedAtom)

            val updatedPreviewAtom = if (publishedAtom.isDefined) {
              previewAtom.copy(
                blockAds = adSettings.blockAds,
                privacyStatus = Some(status)
              )
            } else {
              // on first publish, set YouTube title and description to that of the Atom
              // this is because there's no guarantee that the YouTube furniture gets subbed before publication and can result in draft furniture being used
              previewAtom.copy(
                blockAds = adSettings.blockAds,
                privacyStatus = Some(status),
                youtubeTitle = previewAtom.title,
                youtubeDescription =
                  YoutubeDescription.clean(previewAtom.description)
              )
            }

            updateYouTube(publishedAtom, updatedPreviewAtom, asset).map {
              atomWithYoutubeUpdates =>
                publish(atomWithYoutubeUpdates, user)
            }
          case _ => Future.successful(publish(previewAtom, user))
        }
      }
    }
  }

  private def getPublishedAtom(): Option[MediaAtom] = {
    try {
      val thriftPublishedAtom = getPublishedAtom(id)
      Some(MediaAtom.fromThrift(thriftPublishedAtom))
    } catch {
      case _: Throwable => None
    }
  }

  private def getResultingPrivacyStatus(
      previewAtom: MediaAtom,
      maybePublishedAtom: Option[MediaAtom]
  ): PrivacyStatus = {
    val atomToTakePrivacyStatusFrom =
      if (userCanMakeVideoPublic(previewAtom, user)) previewAtom
      else maybePublishedAtom.getOrElse(previewAtom)

    atomToTakePrivacyStatusFrom.privacyStatus.getOrElse(PrivacyStatus.Unlisted)
  }

  private def userCanMakeVideoPublic(
      atom: MediaAtom,
      user: PandaUser
  ): Boolean =
    !atom.channelId.exists(youtube.channelsRequiringPermission) ||
      permissionsProvider
        .getStatusPermissions(user)
        .setVideosOnAllChannelsPublic

  private def publish(atom: MediaAtom, user: PandaUser): MediaAtom = {
    log.info(s"Publishing atom $id")

    val changeRecord = Some(ChangeRecord.now(user))

    val updatedAtom = atom.copy(
      contentChangeDetails = atom.contentChangeDetails.copy(
        published = changeRecord,
        lastModified = changeRecord,
        scheduledLaunch = None,
        embargo = None
      )
    )

    AuditMessage(id, "Publish", getUsername(user)).logMessage()
    val updatedAtomToPublish =
      UpdateAtomCommand(id, updatedAtom, stores, user, awsConfig).process()

    val publishedAtom = publishAtomToLive(updatedAtomToPublish)
    updateInactiveAssets(publishedAtom)
    publishedAtom
  }

  private def publishAtomToLive(mediaAtom: MediaAtom): MediaAtom = {
    val atom = mediaAtom.asThrift
    val event = ContentAtomEvent(atom, EventType.Update, (new Date()).getTime())

    livePublisher.publishAtomEvent(event) match {
      case Success(_) =>
        publishedDataStore.updateAtom(atom) match {
          case Right(_) => {
            log.info(
              s"Successfully published atom: ${id} (revision ${atom.contentChangeDetails.revision})"
            )
            MediaAtom.fromThrift(atom)
          }
          case Left(err) =>
            log.error("Unable to update datastore after publish", err)
            AtomPublishFailed(s"Could not save published atom")
        }
      case Failure(err) =>
        log.error("Unable to publish atom to kinesis", err)
        AtomPublishFailed(s"Could not publish atom")
    }
  }

  private def updateYouTube(
      publishedAtom: Option[MediaAtom],
      previewAtom: MediaAtom,
      asset: Asset
  ): Future[MediaAtom] = {
    previewAtom.channelId match {
      case Some(channel) if youtube.allChannels.contains(channel) =>
        if (youtube.usePartnerApi) {
          createOrUpdateYoutubeClaim(publishedAtom, previewAtom, asset)
        }
        updateYoutubeMetadata(previewAtom, asset)
        updateYoutubeThumbnail(previewAtom, asset).recover {
          case e: Throwable =>
            log.error("failed to update thumbnail; skipping", e)
            previewAtom
        }

      case Some(_) =>
        // third party YouTube video that we do not have permission to edit
        Future.successful(previewAtom)

      case None if youtube.cannotReachYoutube =>
        // the atom will be missing a channel because we couldn't query YouTube at all
        Future.successful(previewAtom)

      case None =>
        AtomPublishFailed("Atom missing YouTube channel")
    }
  }

  private def hasNewAssets(
      previewAtom: MediaAtom,
      publishedAtom: MediaAtom
  ): Boolean = {
    val previewVersion = previewAtom.activeVersion.get

    publishedAtom.activeVersion match {
      case None => true
      case Some(publishedVersion) => {
        publishedVersion != previewVersion
      }
    }
  }

  private def createOrUpdateYoutubeClaim(
      maybePublishedAtom: Option[MediaAtom],
      previewAtom: MediaAtom,
      asset: Asset
  ): Future[MediaAtom] = Future {
    maybePublishedAtom match {
      case Some(publishedAtom) => {
        if (
          !hasNewAssets(
            previewAtom,
            publishedAtom
          ) && (previewAtom.blockAds == publishedAtom.blockAds)
        ) {
          YouTubeMessage(
            previewAtom.id,
            "N/A",
            "Claim Update",
            "No change to assets or BlockAds field, not editing YouTube Claim"
          ).logMessage()
          previewAtom
        } else {
          previewAtom.category match {
            case Category.Hosted | Category.Paid => {
              val claimUpdate = youtube.createOrUpdateClaim(
                previewAtom.id,
                asset.id,
                AdSettings.NONE
              )
              handleYouTubeMessages(
                claimUpdate,
                "YouTube Claim Update: Block ads on Glabs atom",
                previewAtom,
                asset.id
              )
            }
            case _ => {
              val adSettings = AdSettings(
                youtube.minDurationForAds,
                youtube.minDurationForMidroll,
                previewAtom
              )
              val activeAssetClaimUpdate = youtube.createOrUpdateClaim(
                previewAtom.id,
                asset.id,
                adSettings
              )
              handleYouTubeMessages(
                activeAssetClaimUpdate,
                "YouTube Claim Update: block ads updated",
                previewAtom,
                asset.id
              )
              val oldActiveAsset = publishedAtom.getActiveAsset().get
              val oldActiveAssetClaimUpdate = youtube.createOrUpdateClaim(
                previewAtom.id,
                oldActiveAsset.id,
                AdSettings.NONE
              )
              handleYouTubeMessages(
                oldActiveAssetClaimUpdate,
                "YouTube Claim Update: ads blocked on previous active asset",
                previewAtom,
                oldActiveAsset.id
              )
            }
          }
        }
      }
      // atom hasn't been published yet
      case _ => {
        val adSettings = AdSettings(
          youtube.minDurationForAds,
          youtube.minDurationForMidroll,
          previewAtom
        )
        val claimUpdate =
          youtube.createOrUpdateClaim(previewAtom.id, asset.id, adSettings)
        handleYouTubeMessages(
          claimUpdate,
          "YouTube Claim Update: creating a claim",
          previewAtom,
          asset.id
        )
      }
    }
  }

  private def updateYoutubeMetadata(
      previewAtom: MediaAtom,
      asset: Asset
  ): MediaAtom = {
    val metadata = YouTubeMetadataUpdate(
      title = Some(previewAtom.youtubeTitle),
      categoryId = previewAtom.youtubeCategoryId,
      description = previewAtom.youtubeDescription,
      tags = previewAtom.tags,
      license = previewAtom.license,
      privacyStatus = previewAtom.privacyStatus.map(_.name)
    ).withSaneTitle()

    val initialYouTubeMetadataUpdate: Either[VideoUpdateError, String] =
      youtube.updateMetadata(
        asset.id,
        metadata
      )

    def abTestError(err: VideoUpdateError) = err.errorToLog.contains(
      "UPDATE_TITLE_NOT_ALLOWED_DURING_TEST_AND_COMPARE"
    )

    val shouldRetry = initialYouTubeMetadataUpdate match {
      case Left(err) if abTestError(err) => true
      case _                             => false
    }

    val finalYoutubeMetadataUpdate = if (shouldRetry) {
      // Updating metadata might fail if A/B testing is turned on for title.
      // This is expected behaviour for editorial as they use this functionality fairly regularly.
      // Retry the metadata update _without_ attempting to update the title and see if that helps
      youtube.updateMetadata(asset.id, metadata.copy(title = None))
    } else {
      // otherwise skip retrying and use the original metadata update request
      initialYouTubeMetadataUpdate
    }

    handleYouTubeMessages(
      finalYoutubeMetadataUpdate,
      "YouTube Metadata Update",
      previewAtom,
      asset.id
    )
  }

  private def setYoutubeThumbnail(
      atom: MediaAtom,
      image: Image,
      asset: Asset
  ): MediaAtom = {

    val thumbnail = (for {
      channelId <- atom.channelId
      channelSettings <- youtube.channelConfig.get(channelId)
      logoPath <- channelSettings.logo
    } yield thumbnailGenerator.getBrandedThumbnail(image, atom.id, logoPath))
      .getOrElse(thumbnailGenerator.getThumbnail(image))

    val thumbnailUpdate = youtube.updateThumbnail(asset.id, thumbnail)

    handleYouTubeMessages(
      thumbnailUpdate,
      "YouTube Thumbnail Update",
      atom,
      asset.id
    )
  }

  private def updateYoutubeThumbnail(
      atom: MediaAtom,
      asset: Asset
  ): Future[MediaAtom] = Future {
    (atom.youtubeOverrideImage, atom.posterImage) match {
      case (Some(youtubeOverrideImage), _) =>
        setYoutubeThumbnail(atom, youtubeOverrideImage, asset)
      case (None, Some(posterImage)) =>
        setYoutubeThumbnail(atom, posterImage, asset)
      case (None, None) => atom
    }
  }

  private def updateInactiveAssets(atom: MediaAtom): Unit = {

    atom.getActiveYouTubeAsset().foreach { activeAsset =>
      val youTubeAssets = atom.assets.filter(_.platform == Youtube)
      val inactiveAssets = youTubeAssets.filterNot(_.id == activeAsset.id)

      // TODO be better! Use the correct type rather than converting to the right type
      val status = PrivacyStatus.Private.asThrift.get

      inactiveAssets.foreach { asset =>
        val privacyStatusUpdate = youtube.setStatus(asset.id, status)
        handleYouTubeMessages(
          privacyStatusUpdate,
          "YouTube Privacy Status Update",
          atom,
          asset.id
        )
      }
    }
  }

  private def handleYouTubeMessages(
      message: Either[VideoUpdateError, String],
      updateType: String,
      atom: MediaAtom,
      assetId: String
  ): MediaAtom = {
    message match {
      case Right(okMessage: String) => {
        YouTubeMessage(atom.id, assetId, updateType, okMessage).logMessage()
        atom
      }
      case Left(error: VideoUpdateError) => {
        YouTubeMessage(
          atom.id,
          assetId,
          updateType,
          error.errorToLog,
          isError = true
        ).logMessage()
        AtomPublishFailed(s"Error in $updateType: ${error.getErrorToClient()}")
      }
    }

  }
}
