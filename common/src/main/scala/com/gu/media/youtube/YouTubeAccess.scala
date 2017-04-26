package com.gu.media.youtube

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.{YouTube => YouTubeClient, YouTubeScopes}
import com.google.api.services.youtubePartner.YouTubePartner
import com.gu.media.Settings
import scala.collection.JavaConversions._

import scala.collection.JavaConverters._

trait YouTubeAccess extends Settings {
  def appName: String = getMandatoryString("name")
  def contentOwner: String = getMandatoryString("youtube.contentOwner")
  val allowedChannels: List[String] = getStringList("youtube.allowedChannels")
  val disallowedVideos: List[String] = getStringList("youtube.disallowedVideos")

  def clientId = getMandatoryString("youtube.clientId")
  def clientSecret = getMandatoryString("youtube.clientSecret")
  def refreshToken = getMandatoryString("youtube.refreshToken")

  def monetizationPolicyId = getMandatoryString("youtube.monetizationPolicyId")
  def trackingPolicyId = getMandatoryString("youtube.trackingPolicyId")

  private val httpTransport = new NetHttpTransport()
  private val jacksonFactory = new JacksonFactory()

  private val credentials: GoogleCredential = new GoogleCredential.Builder()
    .setTransport(httpTransport)
    .setJsonFactory(jacksonFactory)
    .setClientSecrets(clientId, clientSecret)
    .build
    .setRefreshToken(refreshToken)

  private val partnerCredentials = credentials.createScoped(List(YouTubeScopes.YOUTUBEPARTNER))

  // lazy to avoid initialising when in test
  lazy val client: YouTubeClient = new YouTubeClient.Builder(httpTransport, jacksonFactory, credentials)
    .setApplicationName(appName)
    .build

  lazy val partnerClient: YouTubePartner = new YouTubePartner.Builder(httpTransport, jacksonFactory, partnerCredentials)
    .setApplicationName(appName)
    .build()

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

  def accessToken(): String = {
    credentials.refreshToken()
    credentials.getAccessToken
  }
}
