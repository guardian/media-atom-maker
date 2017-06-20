package model.commands

import java.net.URL
import java.time.Instant
import java.util.Date

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes._
import play.api.libs.json.{JsValue, JsLookupResult}

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import com.gu.media.youtube.{YouTube, YouTubeClaims, YouTubeMetadataUpdate}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.Platform.Youtube
import model._
import model.commands.CommandExceptions._
import com.gu.media.Capi

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

case class PublishAtomCommand(id: String, override val stores: DataStores, youTube: YouTube, youtubeClaims: YouTubeClaims,
                              user: PandaUser, val capi: Capi)
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
        val atomWithDuration = previewAtom.copy(duration = youTube.getDuration(asset.id))
        updateYouTube(atomWithDuration, asset).map(atomWithYoutubeUpdates => {
          publish(atomWithYoutubeUpdates, user)
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

    auditDataStore.auditPublish(id, user)
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
    updateYoutubeMetadata(previewAtom, asset)
    updateYoutubeThumbnail(previewAtom, asset)
    createOrUpdateYoutubeClaim(previewAtom, asset)
  }

  private def createOrUpdateYoutubeClaim(previewAtom: MediaAtom, asset: Asset): Future[MediaAtom] = Future{
    // if previewAtom.blockAds.isEmpty == true, we know there isn't a published atom and we can save a database call
    if (previewAtom.blockAds.isEmpty) {
      log.info(s"BlockAds not previously set, defaulting to false")
      youtubeClaims.createOrUpdateClaim(
        previewAtom.id,
        asset.id,
        blockAds = false
      )

      previewAtom.copy(blockAds = Some(false))
    }
    else {
      try {
        val thriftPublishedAtom = getPublishedAtom(id)
        val publishedAtom = MediaAtom.fromThrift(thriftPublishedAtom)

        if (previewAtom.blockAds.get == publishedAtom.blockAds.getOrElse(false)) {
          log.info(s"No change to BlockAds field, not editing YouTube Claim")
        } else {
          log.info(s"BlockAds changed from ${publishedAtom.blockAds.getOrElse(false)} to ${previewAtom.blockAds.get}. Updating YouTube Claim")
          youtubeClaims.createOrUpdateClaim(
            previewAtom.id,
            asset.id,
            previewAtom.blockAds.get
          )
        }
        previewAtom
      } catch {
        case CommandException(_, 404) => {
          // atom hasn't been published yet
          log.info(s"Unable to find Published atom. Creating YouTube Claim")
          youtubeClaims.createOrUpdateClaim(
            previewAtom.id,
            asset.id,
            previewAtom.blockAds.get
          )
          previewAtom
        }
      }
    }
  }

  private def removeHtmlTagsForYouTube(description: String): String = {
      val html = Jsoup.parse(description)
      html.select("a").remove()
      html.text()
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
      case Some(page) => "\n View the video at https://www.theguardian.com/" + page
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
      privacyStatus = previewAtom.privacyStatus.map(_.name))
      .withSaneTitle()
      .withContentBundleTags()

    youTube.updateMetadata(asset.id, metadata)

    previewAtom
  }

  private def updateYoutubeThumbnail(atom: MediaAtom, asset: Asset): Future[MediaAtom] = Future{
    try {
      val master = atom.posterImage.flatMap(_.master).get
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

      youTube.updateThumbnail(asset.id, new URL(img.file), img.mimeType.get)
      atom
    } catch {
      case e: GoogleJsonResponseException if e.getDetails.getCode == 503 => YouTubeConnectionIssue
      case NonFatal(e) =>
        log.error(s"Unable to update thumbnail for asset=${asset.id} atom={$id}", e)
        PosterImageUploadFailed(e.getMessage)
    }
  }

  private def updateInactiveAssets(atom: MediaAtom): Unit = {
    val nowMillis = Instant.now().toEpochMilli
    val expired = atom.expiryDate.exists(_ < nowMillis)

    MediaAtom.getActiveYouTubeAsset(atom).foreach { activeAsset =>
      val youTubeAssets = atom.assets.filter(_.platform == Youtube)
      val inactiveAssets = youTubeAssets.filterNot(_.id == activeAsset.id)

      val status = if(expired) { "Private" } else { "Unlisted" }

      inactiveAssets.foreach { asset =>
        log.info(s"Marking asset=${asset.id} atom=${atom.id} as private")
        youTube.setStatus(asset.id, status)
      }
    }
  }

  private def getActiveAsset(atom: MediaAtom): Option[Asset] = for {
    version <- atom.activeVersion
    asset <- atom.assets.find(_.version == version)
  } yield asset
}
