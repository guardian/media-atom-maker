package com.gu.media.youtube

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.{YouTube => YouTubeClient}
import com.gu.media.Settings

import scala.collection.JavaConverters._

trait YouTubeAccess extends Settings {
  val appName: String = getMandatoryString("name")
  val contentOwner: String = getMandatoryString("youtube.contentOwner")
  val allowedChannels: List[String] = getStringList("youtube.allowedChannels")
  val disallowedVideos: List[String] = getStringList("youtube.disallowedVideos")

  private val clientId = getMandatoryString("youtube.clientId")
  private val clientSecret = getMandatoryString("youtube.clientSecret")
  private val refreshToken = getMandatoryString("youtube.refreshToken")

  private val httpTransport = new NetHttpTransport()
  private val jacksonFactory = new JacksonFactory()

  private val credentials: GoogleCredential = new GoogleCredential.Builder()
    .setTransport(httpTransport)
    .setJsonFactory(jacksonFactory)
    .setClientSecrets(clientId, clientSecret)
    .build
    .setRefreshToken(refreshToken)

  val client: YouTubeClient = new YouTubeClient.Builder(httpTransport, jacksonFactory, credentials)
    .setApplicationName(appName)
    .build

  def categories: List[YouTubeVideoCategory] = {
    val request = client.videoCategories()
      .list("snippet")
      .setRegionCode("GB")

    request.execute.getItems.asScala.toList
      .filter(_.getSnippet.getAssignable)
      .map(YouTubeVideoCategory.build)
      .sortBy(_.title)
  }

  def channels: List[YouTubeChannel] = {
    val request = client.channels()
      .list("snippet")
      .setMaxResults(50L)
      .setManagedByMe(true)
      .setOnBehalfOfContentOwner(contentOwner)

    val allChannels = request.execute().getItems.asScala.toList.map(YouTubeChannel.build).sortBy(_.title)

    allowedChannels match {
      case Nil => allChannels
      case allowedList => allChannels.filter(c => allowedList.contains(c.id))
    }
  }
}
