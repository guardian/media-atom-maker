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
import util.YouTube

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class PublishAtomCommand(
  id: String,
  override val stores: DataStores,
  youtube: YouTube,
  user: PandaUser,
  capi: Capi,
  permissions: MediaAtomMakerPermissionsProvider)
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

    getActiveAsset(previewAtom) match {
      case Some(asset) if asset.platform == Youtube =>
        val duration = youtube.getDuration(asset.id)

        val blockAds = previewAtom.category match {
          case Category.Hosted | Category.Paid => {
            true
          }
          case _ => {
            if (duration.getOrElse(0L) < youtube.minDurationForAds) true else previewAtom.blockAds
          }
        }

        val privacyStatus: Future[PrivacyStatus] = (previewAtom.channelId, previewAtom.privacyStatus) match {
          case (Some(channel), Some(status)) if youtube.unlistedWithoutPermissionChannels.contains(channel) && status == PrivacyStatus.Public => {
            permissions.getStatusPermissions(user.email).map(permissions => {
              val hasMakePublicPermission = permissions.setVideosOnAllChannelsPublic

              if (hasMakePublicPermission){
                PrivacyStatus.Public
              } else {
                log.info(s"User ${user.email} does not have permission to publish atom ${previewAtom.id} as Public, setting as Unlisted")
                PrivacyStatus.Unlisted
              }
            })
          }
          case (_, _) => Future.successful(previewAtom.privacyStatus.getOrElse(PrivacyStatus.Unlisted))
        }

        privacyStatus.flatMap(status => {
          val updatedPreviewAtom = previewAtom.copy(duration = duration, blockAds = blockAds, privacyStatus = Some(status))
          updateYouTube(updatedPreviewAtom, asset).map(atomWithYoutubeUpdates => {
            publish(atomWithYoutubeUpdates, user)
          })
        })
      case _ =>
        Future.successful(publish(previewAtom, user))
    }
  }

  private def publish(atom: MediaAtom, user: PandaUser): MediaAtom = {
    log.info(s"Publishing atom $id")

    val changeRecord = Some(ChangeRecord.now(user))

    val updatedAtom = atom.copy(
      contentChangeDetails = atom.contentChangeDetails.copy(
        published = changeRecord,
        lastModified = changeRecord,
        revision = atom.contentChangeDetails.revision + 1
      )
    )

    auditDataStore.auditPublish(id, getUsername(user))
    UpdateAtomCommand(id, updatedAtom, stores, user).process()

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
            AtomPublishFailed(s"Could not update published datastore after publish: ${err.toString}")
        }
      case Failure(err) =>
        log.error("Unable to publish atom to kinesis", err)
        AtomPublishFailed(s"Could not publish atom (live kinesis event failed): ${err.toString}")
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

  private def noClaimsToUpdate(previewAtom: MediaAtom, publishedAtom: MediaAtom): Boolean = {

    val previewVersion = previewAtom.activeVersion.get
    val noNewAssets = publishedAtom.activeVersion match {
      case None => false
      case Some(publishedVersion) => {
        publishedVersion == previewVersion
      }
    }
    previewAtom.blockAds == publishedAtom.blockAds && noNewAssets
  }

  private def createOrUpdateYoutubeClaim(previewAtom: MediaAtom, asset: Asset): Future[MediaAtom] = Future{
    try {
      val thriftPublishedAtom = getPublishedAtom(id)
      val publishedAtom = MediaAtom.fromThrift(thriftPublishedAtom)

      if (noClaimsToUpdate(previewAtom, publishedAtom)) {
        log.info(s"No change to BlockAds field, not editing YouTube Claim")
      } else {
        log.info(s"BlockAds changed from ${publishedAtom.blockAds} to ${previewAtom.blockAds}. Updating YouTube Claim")
        youtube.createOrUpdateClaim(
          previewAtom.id,
          asset.id,
          previewAtom.blockAds
        )
      }
      previewAtom

    } catch {
      case CommandException(_, 404) => {
        // atom hasn't been published yet
        log.info(s"Unable to find Published atom. Creating YouTube Claim")
        youtube.createOrUpdateClaim(
          previewAtom.id,
          asset.id,
          previewAtom.blockAds
        )
        previewAtom
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


    val usages: JsValue = (capi.capiQuery("/atom/media/" + atomId + "/usage", true) \ "response" \ "results").get
    val usagesList = usages.as[List[String]]

    val composerPage = usagesList.find(usage => {
      val query = capi.capiQuery(usage, true)
      val contentType = (capi.capiQuery(usage, true) \ "response" \ "content" \ "type").get.as[String]
      contentType == "video"
    })

    composerPage match {
      case Some(page) => "\nView the video at https://www.theguardian.com/" + page
      case None => ""
    }
  }

  private def updateYoutubeMetadata(previewAtom: MediaAtom, asset: Asset) = {

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

    youtube.updateMetadata(
      asset.id,
      if (previewAtom.blockAds) metadata.withoutContentBundleTags() else metadata.withContentBundleTags() // content bundle tags only needed on monetized videos
    )

    previewAtom
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

        youtube.updateThumbnail(asset.id, new URL(img.file), img.mimeType.get)
        atom
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
        log.info(s"Marking asset=${asset.id} atom=${atom.id} as private")
        youtube.setStatus(asset.id, status)
      }
    }
  }

  private def getActiveAsset(atom: MediaAtom): Option[Asset] = for {
    version <- atom.activeVersion
    asset <- atom.assets.find(_.version == version)
  } yield asset
}
