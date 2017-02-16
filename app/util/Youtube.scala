package util

import java.io.BufferedInputStream
import java.net.URL
import java.time.Duration
import java.util.Date
import javax.inject.{Inject, Singleton}

import com.google.api.client.http.InputStreamContent
import com.google.api.services.youtube.model.Video
import com.gu.contentatom.thrift.Atom
import com.gu.media.YouTubeClient
import com.gu.media.logging.Logging
import model.Platform.Youtube
import model._
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._

@Singleton
class YouTubeConfig @Inject()(config: Configuration) {
  val YouTubeClient(client, _, contentOwner, allowedChannels, disallowedVideos) = YouTubeClient(config.underlying)
}

case class YouTubeVideoCategoryApi(config: YouTubeConfig) {
  def list: List[YouTubeVideoCategory] = {
    val request = config.client.videoCategories()
      .list("snippet")
      .setRegionCode("GB")

    request.execute.getItems.asScala.toList
      .filter(_.getSnippet.getAssignable)
      .map(YouTubeVideoCategory.build)
      .sortBy(_.title)
  }
}

case class YouTubeVideoUpdateApi(config: YouTubeConfig) extends Logging {
  private def protectAgainstMistakesInDev(video: Video) = {
    val videoChannelId = video.getSnippet.getChannelId

    if (config.disallowedVideos.contains(video.getId)) {
      val msg = s"Failed to edit video ${video.getId} as its in config.youtube.disallowedVideos"
      Logger.info(msg)
      throw new Exception(msg)
    }

    if (!config.allowedChannels.contains(videoChannelId)) {
      val msg = s"Failed to edit video ${video.getId} as its channel ($videoChannelId) isn't in config.youtube.allowedChannels"
      Logger.info(msg)
      throw new Exception(msg)
    }
  }

  def updateMetadata(id: String, metadata: UpdatedMetadata): Option[Video] =
    YouTubeVideoInfoApi(config).getVideo(id, "snippet, status") match {
      case Some(video) =>
        protectAgainstMistakesInDev(video)

        val snippet = video.getSnippet
        val status = video.getStatus

        metadata.title match {
          case Some(title) => snippet.setTitle(title)
          case _ => None
        }

        metadata.categoryId match {
          case Some(cat) => snippet.setCategoryId(cat)
          case _ => None
        }

        metadata.description match {
          case Some(desc) => snippet.setDescription(desc)
          case _ => None
        }

        metadata.tags match {
          case Some(t) => snippet.setTags(t.asJava)
          case _ => None
        }

        metadata.license match {
          case Some(l) => status.setLicense(l)
          case _ => None
        }

        metadata.privacyStatus match {
          case Some(ps) => status.setPrivacyStatus(ps.name.toLowerCase)
          case _ => None
        }

        video.setSnippet(snippet)
        video.setStatus(status)

        log.info(s"Updating YouTube metadata for $id:\n${UpdatedMetadata.prettyToString(metadata)}")

        Some(config.client.videos()
          .update("snippet, status", video)
          .setOnBehalfOfContentOwner(config.contentOwner)
          .execute())
      case _ => None
    }

  def updateStatusIfExpired(thriftAtom: Atom): Option[MediaAtom] = {

    val atom: MediaAtom = MediaAtom.fromThrift(thriftAtom)
    val atomId = atom.id
    val timeNow = new Date().getTime

    atom.expiryDate match {
      case Some(date) => {

        if (date <= timeNow && atom.privacyStatus.get != PrivacyStatus.Private) {
          atom.assets.collect {
            case asset if asset.platform == Youtube =>
              log.info(s"Marking asset=${asset.id} atom=$atomId as private due to expiry")
              setStatusToPrivate(asset.id, atomId)
          }

          val updatedAtom = atom.copy(privacyStatus = Some(PrivacyStatus.Private))
          Some(updatedAtom)

        } else None

      }
      case _ => None
    }
  }

  def setStatusToPrivate(id: String, atomId: String): Unit = {
    YouTubeVideoInfoApi(config).getVideo(id, "snippet,status") match {
      case Some(video) => {
        protectAgainstMistakesInDev(video)

        video.getStatus.setPrivacyStatus(PrivacyStatus.Private.name)

        try {
          Some(config.client.videos()
            .update("snippet, status", video)
            .setOnBehalfOfContentOwner(config.contentOwner)
            .execute())

          log.info(s"marked asset=$id atom=$atomId as private")
        }
        catch {
          case e: Throwable =>
            log.warn(s"unable to mark asset=$id atom=$atomId as private", e)
        }
      }
      case _ =>
    }
  }

  def updateThumbnail(id: String, thumbnailUrl: URL, mimeType: String): Option[Video] = {
    YouTubeVideoInfoApi(config).getVideo(id, "snippet") match {
      case Some(video) => {
        protectAgainstMistakesInDev(video)

        val content = new InputStreamContent(mimeType, new BufferedInputStream(thumbnailUrl.openStream()))
        val set = config.client.thumbnails().set(id, content).setOnBehalfOfContentOwner(config.contentOwner)

        // If we want some way of monitoring and resuming thumbnail uploads then we can change this to be `false`
        set.getMediaHttpUploader.setDirectUploadEnabled(true)
        set.execute()

        Some(video)
      }
      case _ => None
    }
  }
}

case class YouTubeChannelsApi(config: YouTubeConfig) {
  def fetchMyChannels(): List[YouTubeChannel] = {
    val request = config.client.channels()
      .list("snippet")
      .setMaxResults(50L)
      .setManagedByMe(true)
      .setOnBehalfOfContentOwner(config.contentOwner)

    val allChannels = request.execute().getItems.asScala.toList.map(YouTubeChannel.build).sortBy(_.title)

    config.allowedChannels match {
      case Nil => allChannels
      case allowedList => allChannels.filter(c => allowedList.contains(c.id))
    }
  }
}

case class YouTubeVideoInfoApi(config: YouTubeConfig) {
  def getVideo(youtubeId: String, part: String): Option[Video] = {
    config.client.videos()
      .list(part)
      .setId(youtubeId)
      .setOnBehalfOfContentOwner(config.contentOwner)
      .execute()
      .getItems.asScala.toList.headOption
  }

  def getProcessingStatus(youtubeId: String): Option[String] =
    getVideo(youtubeId, "processingDetails") match {
      case Some(video) => Some(video.getProcessingDetails.getProcessingStatus)
      case None => None
    }

  def getDuration(youtubeId: String): Option[Long] = {
    getVideo(youtubeId, "contentDetails") match {
      case Some(video) => {
        // YouTube API returns duration is in ISO 8601 format
        // https://developers.google.com/youtube/v3/docs/videos#contentDetails.duration
        val iso8601Duration = video.getContentDetails.getDuration

        Some(Duration.parse(iso8601Duration).toMillis / 1000) // seconds
      }
      case None => None
    }
  }

  def isMyVideo(youtubeId: String): Boolean = {
    // HACK Listing `fileDetails` of a video requires authentication and an exception is thrown if not authorized,
    // so we can say the video is not ours.
    //
    // A cleaner way to do this would be to check the channel id of the video is one of ours,
    // however this involves an extra API call.
    // TODO cache YouTube channels in a store and query against that.
    try {
      getVideo(youtubeId, "fileDetails").nonEmpty
    }
    catch {
      case _: Throwable => false
    }
  }
}
