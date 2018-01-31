package model.commands

import java.net.URL
import java.time.Instant
import java.util.Date

import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.{Capi, MediaAtomMakerPermissionsProvider}
import com.gu.media.logging.Logging
import com.gu.media.model._
import com.gu.media.youtube.YouTubeMetadataUpdate
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import com.gu.media.model.Platform.Youtube
import model._
import model.commands.CommandExceptions._
import org.jsoup.Jsoup
import play.api.libs.json.JsValue
import util.{AWSConfig, YouTube}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class PublishAtomCommand(
  id: String,
  override val stores: DataStores,
  youtube: YouTube,
  user: PandaUser,
  capi: Capi,
  permissions: MediaAtomMakerPermissionsProvider,
  awsConfig: AWSConfig)
  extends Command with AtomAPIActions with Logging {

  type T = Future[MediaAtom]

  def process(): T = {
    log.info(s"Request to publish atom $id")

    val thriftPreviewAtom = getPreviewAtom(id)
    val previewAtom = MediaAtom.fromThrift(thriftPreviewAtom)

    if(previewAtom.privacyStatus.contains(PrivacyStatus.Private)) {
      log.error(s"Unable to publish atom ${previewAtom.id}, privacy status is set to private")
      AtomPublishFailed("Atom status set to private")
    }

    val contentChangeDetails = thriftPreviewAtom.contentChangeDetails
    val now = Instant.now().toEpochMilli

    (contentChangeDetails.expiry, contentChangeDetails.scheduledLaunch, contentChangeDetails.embargo) match {
      case (Some(expiry), _, _) if expiry.date <= now => {
        log.error(s"Unable to publish expired atom. atom=${previewAtom.id} expiry=${expiry.date}")
        AtomPublishFailed("Atom has expired")
      }
      case (_, _, Some(embargo)) if embargo.date > now => {
        log.error(s"Unable to publish atom with embargo date. atom=${previewAtom.id} embargo=${embargo.date}")
        AtomPublishFailed("Atom embargoed")
      }
      case (_, Some(schedule), _) if schedule.date > now => {
        log.error(s"Unable to publish atom as schedule time in the future. atom=${previewAtom.id} schedule=${schedule.date} now=$now")
        AtomPublishFailed("Atom scheduled for the future")
      }
      case (_, Some(schedule), Some(embargo)) if schedule.date < embargo.date => {
        log.error(s"Unable to publish atom as embargoed after schedule. atom=${previewAtom.id} schedule=${schedule.date} embargo=${embargo.date}")
        AtomPublishFailed("Embargo set after schedule")
      }
      case (_, _, _) => {
        getActiveAsset(previewAtom) match {
          case Some(asset) if asset.platform == Youtube =>
            val blockAds = getAtomBlockAds(previewAtom)
            val privacyStatus: Future[PrivacyStatus] = getPrivacyStatus(previewAtom)

            privacyStatus.flatMap(status => {
              val updatedPreviewAtom = previewAtom.copy(blockAds = blockAds, privacyStatus = Some(status))
              updateYouTube(updatedPreviewAtom, asset).map(atomWithYoutubeUpdates => {
                publish(atomWithYoutubeUpdates, user)
              })
            })
          case _ => Future.successful(publish(previewAtom, user))
        }
      }
    }

  }

  private def getAtomBlockAds(previewAtom: MediaAtom): Boolean = {
    previewAtom.category match {
      // GLabs atoms will always have ads blocked on YouTube,
      // so the thrift field maps to the Composer page and we don't need to check the video duration
      case Category.Hosted | Category.Paid => previewAtom.blockAds
      case _ => if (previewAtom.duration.getOrElse(0L) < youtube.minDurationForAds) true else previewAtom.blockAds
    }
  }

  private def getPrivacyStatus(previewAtom: MediaAtom) = {
    val privacyStatus: Future[PrivacyStatus] = (previewAtom.channelId, previewAtom.privacyStatus) match {
      case (Some(channel), Some(status)) if youtube.unlistedWithoutPermissionChannels.contains(channel) && status == PrivacyStatus.Public => {
        permissions.getStatusPermissions(user).map(permissions => {
          val hasMakePublicPermission = permissions.setVideosOnAllChannelsPublic

          if (hasMakePublicPermission) {
            PrivacyStatus.Public
          } else {
            log.info(s"User ${user.email} does not have permission to publish atom ${previewAtom.id} as Public, setting as Unlisted")
            PrivacyStatus.Unlisted
          }
        })
      }
      case (_, _) => Future.successful(previewAtom.privacyStatus.getOrElse(PrivacyStatus.Unlisted))
    }
    privacyStatus
  }

  private def publish(atom: MediaAtom, user: PandaUser): MediaAtom = {
    log.info(s"Publishing atom $id")

    val changeRecord = Some(ChangeRecord.now(user))

    val updatedAtom = atom.copy(
      contentChangeDetails = atom.contentChangeDetails.copy(
        published = changeRecord,
        lastModified = changeRecord,
        revision = atom.contentChangeDetails.revision + 1,
        scheduledLaunch = None,
        embargo = None
      )
    )

    auditDataStore.auditPublish(id, getUsername(user))
    UpdateAtomCommand(id, updatedAtom, stores, user, awsConfig).process()

    val publishedAtom = publishAtomToLive(updatedAtom)
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
            log.info(s"Successfully published atom: ${id}")
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

  private def updateYouTube(previewAtom: MediaAtom, asset: Asset): Future[MediaAtom] = {
    previewAtom.channelId match {
      case Some(channel) if youtube.allChannels.contains(channel) =>
        if (youtube.usePartnerApi) {
          createOrUpdateYoutubeClaim(previewAtom, asset)
        }
        updateYoutubeMetadata(previewAtom, asset)
        updateYoutubeThumbnail(previewAtom, asset)
      case Some(_) =>
        // third party YouTube video that we do not have permission to edit
        Future.successful(previewAtom)

      case None =>
        AtomPublishFailed("Atom missing YouTube channel")
    }
  }

  private def hasNewAssets(previewAtom: MediaAtom, publishedAtom: MediaAtom): Boolean = {
    val previewVersion = previewAtom.activeVersion.get

    publishedAtom.activeVersion match {
      case None => true
      case Some(publishedVersion) => {
        publishedVersion != previewVersion
      }
    }
  }

  private def createOrUpdateYoutubeClaim(previewAtom: MediaAtom, asset: Asset): Future[MediaAtom] = Future{
    try {
      val thriftPublishedAtom = getPublishedAtom(id)
      val publishedAtom = MediaAtom.fromThrift(thriftPublishedAtom)

      if (!hasNewAssets(previewAtom, publishedAtom)) {
        YouTubeMessage(previewAtom.id, "N/A", "Claim Update", "No change to assets, not editing YouTube Claim").logMessage
        previewAtom
      } else {
        previewAtom.category match {
          case Category.Hosted | Category.Paid => {
            val claimUpdate = youtube.createOrUpdateClaim(previewAtom.id, asset.id, blockAds = true)
            handleYouTubeMessages(claimUpdate, "YouTube Claim Update: Block ads on Glabs atom", previewAtom, asset.id)
          }
          case _ => {
            if (previewAtom.blockAds == publishedAtom.blockAds) {
              YouTubeMessage(previewAtom.id, "N/A", "Claim Update", "No change to BlockAds field, not editing YouTube Claim").logMessage
              previewAtom
            } else {
              val claimUpdate = youtube.createOrUpdateClaim(previewAtom.id, asset.id, previewAtom.blockAds)
              handleYouTubeMessages(claimUpdate, "YouTube Claim Update: block ads updated", previewAtom, asset.id)
            }
          }
        }
      }
    } catch {
      case CommandException(_, 404) => {
        // atom hasn't been published yet

        val blockAds = previewAtom.category match {
          case Category.Hosted | Category.Paid => true
          case _ => previewAtom.blockAds
        }

        val claimUpdate = youtube.createOrUpdateClaim(previewAtom.id, asset.id, blockAds)
        handleYouTubeMessages(claimUpdate, "YouTube Claim Update: creatign a claim", previewAtom, asset.id)
      }
    }
  }

  private def removeHtmlTagsForYouTube(description: String): String = {

      val html = Jsoup.parse(description)

      //Extracting the text removes line breaks
      //We add them back in before each paragraph except
      //for the first and before each list element
      html.select("p:gt(0), li").prepend("\\n");
      html.select("a").unwrap()
      val text = html.text()
      html.text().replace("\\n", "\n")
  }

  private def getComposerLinkText(atomId: String): String = {
    val path = s"/atom/media/$atomId/usage"
    val emptyQs: Map[String, String] = Map()

    val usages: JsValue = (capi.capiQuery(path, emptyQs, queryLive = true) \ "response" \ "results").get
    val usagesList = usages.as[List[String]]

    val composerPage = usagesList.find(usage => {
      val contentType = (capi.capiQuery(usage, emptyQs, queryLive = true) \ "response" \ "content" \ "type").get.as[String]
      contentType == "video"
    })

    composerPage match {
      case Some(page) => "\nView the video at https://www.theguardian.com/" + page
      case None => ""
    }
  }

  private def updateYoutubeMetadata(previewAtom: MediaAtom, asset: Asset): MediaAtom = {

    val description = previewAtom.description.map(description => {
      removeHtmlTagsForYouTube(description) + getComposerLinkText(previewAtom.id)
    })

    val metadata = YouTubeMetadataUpdate(
      title = Some(previewAtom.title),
      categoryId = previewAtom.youtubeCategoryId,
      description = description,
      tags = previewAtom.tags,
      license = previewAtom.license,
      privacyStatus = previewAtom.privacyStatus.map(_.name)
    ).withSaneTitle()

    val youTubeMetadataUpdate: Either[VideoUpdateError, String] = youtube.updateMetadata(
      asset.id,
      if (previewAtom.blockAds) metadata.withoutContentBundleTags() else metadata.withContentBundleTags() // content bundle tags only needed on monetized videos
    )

    handleYouTubeMessages(youTubeMetadataUpdate, "YouTube Metadata Update", previewAtom, asset.id)
  }

  private def updateYoutubeThumbnail(atom: MediaAtom, asset: Asset): Future[MediaAtom] = Future{
    atom.posterImage.flatMap(_.master) match {
      case Some(master) => {
        val MAX_SIZE = 2000000
        val img: ImageAsset = if (master.size.get < MAX_SIZE) {
          master
        } else {
          // Get the biggest crop which is still less than MAX_SIZE
          atom.posterImage.map(
            _.assets
              .filter(a => a.size.nonEmpty && a.size.get < MAX_SIZE)
              .maxBy(_.size.get)
          ).get
        }

        val thumbnailUpdate = youtube.updateThumbnail(asset.id, new URL(img.file), img.mimeType.get)

        handleYouTubeMessages(thumbnailUpdate, "YouTube Thumbnail Update", atom, asset.id)
      }
      case None => atom
    }
  }

  private def updateInactiveAssets(atom: MediaAtom): Unit = {

    MediaAtom.getActiveYouTubeAsset(atom).foreach { activeAsset =>
      val youTubeAssets = atom.assets.filter(_.platform == Youtube)
      val inactiveAssets = youTubeAssets.filterNot(_.id == activeAsset.id)

      //TODO be better! Use the correct type rather than converting to the right type
      val status = PrivacyStatus.Private.asThrift.get

      inactiveAssets.foreach { asset =>
        val privacyStatusUpdate = youtube.setStatus(asset.id, status)
        handleYouTubeMessages(privacyStatusUpdate, "YouTube Privacy Status Update", atom, asset.id)
      }
    }
  }

  private def getActiveAsset(atom: MediaAtom): Option[Asset] = for {
    version <- atom.activeVersion
    asset <- atom.assets.find(_.version == version)
  } yield asset

  private def handleYouTubeMessages(message: Either[VideoUpdateError, String], updateType: String, atom: MediaAtom, assetId: String): MediaAtom = {
    message match {
      case Right(okMessage: String) => {
        YouTubeMessage(atom.id, assetId, updateType, okMessage).logMessage
        atom
      }
      case Left(error: VideoUpdateError) => {
        YouTubeMessage(atom.id, assetId, updateType, error.errorToLog, isError = true).logMessage
        AtomPublishFailed(s"Error in $updateType: ${error.getErrorToClient()}")
      }
    }

  }
}
