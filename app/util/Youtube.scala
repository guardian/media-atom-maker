package util

import java.io.BufferedInputStream
import java.net.URL
import java.time.Duration
import javax.inject.{ Singleton, Inject }

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Video
import model.{UpdatedMetadata, YouTubeVideoCategory, YouTubeChannel}
import play.api.Configuration

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
  def updateMetadata(id: String, metadata: UpdatedMetadata): Option[Video] =
      youtube.videos()
        .list("snippet, status")
        .setId(id)
        .execute()
        .getItems.asScala.toList.headOption match {
      case Some(video) =>
        val snippet = video.getSnippet
        snippet.setTitle(snippet.getTitle)
        snippet.setCategoryId(metadata.categoryId.getOrElse(snippet.getCategoryId))
        snippet.setDescription(metadata.description.getOrElse(snippet.getDescription))
        snippet.setTags(metadata.tags.map(_.asJava).getOrElse(snippet.getTags))
        video.setSnippet(snippet)

        val status = video.getStatus
        status.setLicense(metadata.license.getOrElse(status.getLicense))
        video.setStatus(status)

        Some(youtube.videos().update("snippet, status", video).setOnBehalfOfContentOwner(config.contentOwner).execute())
      case _ => None
    }

  def updateThumbnail(id: String, thumbnailUrl: URL, mimeType: String): Unit = {
    val content = new InputStreamContent(mimeType, new BufferedInputStream(thumbnailUrl.openStream()))
    val set = youtube.thumbnails().set(id, content).setOnBehalfOfContentOwner(config.contentOwner)

    // If we want some way of monitoring and resuming thumbnail uploads then we can change this to be `false`
    set.getMediaHttpUploader.setDirectUploadEnabled(true)
    set.execute()
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
  def getProcessingStatus(youtubeId: String): Option[String] =
    youtube.videos()
      .list("processingDetails")
      .setId(youtubeId)
      .setOnBehalfOfContentOwner(config.contentOwner)
      .execute()
      .getItems.asScala.toList.headOption match {
      case Some(video) => Some(video.getProcessingDetails.getProcessingStatus)
      case None => None
    }

  def getDuration(youtubeId: String): Option[Long] = {
    youtube.videos()
      .list("contentDetails")
      .setId(youtubeId)
      .setOnBehalfOfContentOwner(config.contentOwner)
      .execute()
      .getItems.asScala.toList.headOption match {
      case Some(video) => {
        // YouTube API returns duration is in ISO 8601 format
        // https://developers.google.com/youtube/v3/docs/videos#contentDetails.duration
        val iso8601Duration = video.getContentDetails.getDuration

        Some(Duration.parse(iso8601Duration).toMillis / 1000) // seconds
      }
      case None => None
    }
  }
}
