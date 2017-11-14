package com.gu.media.youtube

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.{YouTubeScopes, YouTube => YouTubeClient}
import com.google.api.services.youtubePartner.YouTubePartner
import com.gu.media.Settings

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

trait YouTubeAccess extends Settings {
  def appName: String = getMandatoryString("name")
  def contentOwner: String = getMandatoryString("youtube.contentOwner")

  val allowedChannels: Set[String] = getStringSet("youtube.channels.allowed")
  val strictlyUnlistedChannels: Set[String] = getStringSet("youtube.channels.unlisted")
  val commercialChannels: Set[String] = getStringSet("youtube.channels.commercial")
  val allChannels: Set[String] = allowedChannels ++ strictlyUnlistedChannels ++ commercialChannels

  val trainingChannels: Set[String] = getStringSet("youtube.channels.training")

  val disallowedVideos: Set[String] = getStringSet("youtube.videos.disallowed")
  val usePartnerApi: Boolean = getString("youtube.usePartnerApi").forall(_.toBoolean)

  def clientId = getMandatoryString("youtube.clientId")
  def clientSecret = getMandatoryString("youtube.clientSecret")
  def refreshToken = getMandatoryString("youtube.refreshToken")

  def monetizationPolicyId = getMandatoryString("youtube.monetizationPolicyId")
  def trackingPolicyId = getMandatoryString("youtube.trackingPolicyId")

  def minDurationForAds: Long = getString("youtube.minDurationForAds").getOrElse("30").toLong

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

    request.execute().getItems.asScala.toList
      .map(YouTubeChannel.build(this, _))
  }

  def channelsWithData(setAllVideosPublic: Boolean): List[YouTubeChannelWithData] = {
    channels.map(channel => YouTubeChannelWithData.build(this, channel.id, channel.title, setAllVideosPublic))
      .sortBy(_.title)
  }

  def accessToken(): String = {
    credentials.refreshToken()
    credentials.getAccessToken
  }
}
