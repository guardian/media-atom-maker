package util

import javax.inject.{ Singleton, Inject }

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.{Video, VideoSnippet}
import model.YouTubeVideoCategory
import play.api.Configuration
import play.api.libs.json.JsValue

import scala.collection.JavaConverters._

@Singleton
class YouTubeConfig @Inject()(config: Configuration) {
  lazy val appName = config.getString("name").getOrElse("")

  lazy val clientId = config.getString("youtube.clientId").getOrElse("")
  lazy val clientSecret = config.getString("youtube.clientSecret").getOrElse("")
  lazy val refreshToken = config.getString("youtube.refreshToken").getOrElse("")
  lazy val contentOwner = config.getString("youtube.contentOwner").getOrElse("")
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

  protected val onBehalfOfContentOwner = config.contentOwner
}

case class YouTubeVideoCategoryApi(config: YouTubeConfig) extends YouTubeBuilder {
  def list: List[YouTubeVideoCategory] = {
    val request = youtube.videoCategories()
      .list("snippet")
      .setRegionCode("GB")

    request.execute.getItems.asScala.toList.map(YouTubeVideoCategory.build).sortBy(_.title)
  }
}

case class YouTubeVideoUpdateApi(config: YouTubeConfig) extends YouTubeBuilder {
  def updateMetadata(id: String, metadata: JsValue): Option[Video] = {
    val videosList = youtube.videos().list("snippet").setId(id).execute().getItems.asScala.toList
    videosList.headOption match {
      case Some(oldVideo) =>
        val oldSnippet = oldVideo.getSnippet
        val snippet = new VideoSnippet()
        snippet.setTitle((metadata \ "title").asOpt[String].fold(oldSnippet.getTitle)(identity))
        snippet.setCategoryId((metadata \ "categoryId").asOpt[String].fold(oldSnippet.getCategoryId)(identity))
        snippet.setDescription((metadata \ "description").asOpt[String].fold(oldSnippet.getDescription)(identity))

        oldVideo.setSnippet(snippet)
        val result = youtube.videos().update("snippet", oldVideo).setOnBehalfOfContentOwner(onBehalfOfContentOwner).execute()
        Some(result)
      case _ => None
    }
  }
}
