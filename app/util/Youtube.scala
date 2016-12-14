package util

import java.io.BufferedInputStream
import java.net.URL
import java.time.Duration
import javax.inject.{Inject, Singleton}

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Video
import model._
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._

@Singleton
class YouTubeConfig @Inject()(config: Configuration) {
  lazy val appName = config.getString("name").getOrElse("")

  lazy val clientId = config.getString("youtube.clientId").getOrElse("")
  lazy val clientSecret = config.getString("youtube.clientSecret").getOrElse("")
  lazy val refreshToken = config.getString("youtube.refreshToken").getOrElse("")
  lazy val contentOwner = config.getString("youtube.contentOwner").getOrElse("")
  lazy val allowedChannels = config.getStringList("youtube.allowedChannels")
}

trait YouTubeBuilder {
  def config: YouTubeConfig

  private val httpTransport = new NetHttpTransport()
  private val jacksonFactory = new JacksonFactory()

  private val credentials: GoogleCredential = {
    new GoogleCredential.Builder()
      .setTransport(httpTransport)
      .setJsonFactory(jacksonFactory)
      .setClientSecrets(config.clientId, config.clientSecret)
      .build
      .setRefreshToken(config.refreshToken)
  }

  protected val youtube = {
    new YouTube.Builder(httpTransport, jacksonFactory, credentials)
      .setApplicationName(config.appName)
      .build
  }
}

case class YouTubeVideoCategoryApi(config: YouTubeConfig) extends YouTubeBuilder {
  def list: List[YouTubeVideoCategory] = {
    val request = youtube.videoCategories()
      .list("snippet")
      .setRegionCode("GB")

    request.execute.getItems.asScala.toList
      .filter(_.getSnippet.getAssignable)
      .map(YouTubeVideoCategory.build)
      .sortBy(_.title)
  }
}

case class YouTubeVideoUpdateApi(config: YouTubeConfig) extends YouTubeBuilder {
  private def protectAgainstMistakesInDev(video: Video) = {
    val videoChannelId = video.getSnippet.getChannelId

    config.allowedChannels match {
      case Some(allowedList) => {
        if (!allowedList.contains(videoChannelId)) {
          val msg = s"Failed to edit video ${video.getId} as its channel ($videoChannelId) isn't in config.youtube.allowedChannels"
          Logger.info(msg)
          throw new Exception(msg)
        }
      }
      case None =>
    }
  }

  def updateMetadata(id: String, metadata: UpdatedMetadata): Option[Video] =
    YouTubeVideoInfoApi(config).getVideo(id, "snippet, status") match {
      case Some(video) =>
        protectAgainstMistakesInDev(video)

        val snippet = video.getSnippet
        val status = video.getStatus

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

        Some(youtube.videos()
          .update("snippet, status", video)
          .setOnBehalfOfContentOwner(config.contentOwner)
          .execute())
      case _ => None
    }

  def updateThumbnail(id: String, thumbnailUrl: URL, mimeType: String): Option[Video] = {
    YouTubeVideoInfoApi(config).getVideo(id, "snippet") match {
      case Some(video) => {
        protectAgainstMistakesInDev(video)

        val content = new InputStreamContent(mimeType, new BufferedInputStream(thumbnailUrl.openStream()))
        val set = youtube.thumbnails().set(id, content).setOnBehalfOfContentOwner(config.contentOwner)

        // If we want some way of monitoring and resuming thumbnail uploads then we can change this to be `false`
        set.getMediaHttpUploader.setDirectUploadEnabled(true)
        set.execute()

        Some(video)
      }
      case _ => None
    }
  }
}

case class YouTubeChannelsApi(config: YouTubeConfig) extends YouTubeBuilder {
  def fetchMyChannels(): List[YouTubeChannel] = {
    val request = youtube.channels()
      .list("snippet")
      .setMaxResults(50L)
      .setManagedByMe(true)
      .setOnBehalfOfContentOwner(config.contentOwner)

    val allChannels = request.execute().getItems.asScala.toList.map(YouTubeChannel.build).sortBy(_.title)

    config.allowedChannels match {
      case None => allChannels
      case Some(allowedList) => allChannels.filter(c => allowedList.contains(c.id))
    }
  }
}

case class YouTubeVideoInfoApi(config: YouTubeConfig) extends YouTubeBuilder {
  def getVideo(youtubeId: String, part: String): Option[Video] = {
    youtube.videos()
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
