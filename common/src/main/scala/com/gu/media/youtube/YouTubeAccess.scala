package com.gu.media.youtube

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.{HttpRequest, HttpRequestInitializer}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.{YouTubeRequest, YouTubeRequestInitializer, YouTubeScopes, YouTube => YouTubeClient}
import com.google.api.services.youtubePartner.YouTubePartner
import com.gu.media.Settings
import com.gu.media.logging.{Logging, YoutubeApiType, YoutubeRequestLogger, YoutubeRequestType}
import net.logstash.logback.marker.{LogstashMarker, Markers}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

trait YouTubeAccess extends Settings with Logging {
  def appName: String = getMandatoryString("name")
  def contentOwner: String = getMandatoryString("youtube.contentOwner")

  val cannotReachYoutube: Boolean = getBoolean("youtube.isDown").getOrElse(false)
  val allowedChannels: Set[String] = getStringSet("youtube.channels.allowed")
  val channelsRequiringPermission: Set[String] = getStringSet("youtube.channels.unlisted")
  val commercialChannels: Set[String] = getStringSet("youtube.channels.commercial")
  val allChannels: Set[String] = allowedChannels ++ channelsRequiringPermission ++ commercialChannels

  val trainingChannels: Set[String] = getStringSet("youtube.channels.training")

  val disallowedVideos: Set[String] = getStringSet("youtube.videos.disallowed")
  val usePartnerApi: Boolean = getString("youtube.usePartnerApi").forall(_.toBoolean)

  def clientId = getMandatoryString("youtube.clientId")
  def clientSecret = getMandatoryString("youtube.clientSecret")
  def refreshToken = getMandatoryString("youtube.refreshToken")

  def monetizationPolicyId = getMandatoryString("youtube.monetizationPolicyId")
  def trackingPolicyId = getMandatoryString("youtube.trackingPolicyId")

  def minDurationForAds: Long = getString("youtube.minDurationForAds").getOrElse("30").toLong

  // Videos need to be at least 8 minutes to be eligible for midroll advertising
  // see https://support.google.com/youtube/answer/6175006?hl=en-GB
  val minDurationForMidroll: Long = 8 * 60L

  private val httpTransport = new NetHttpTransport()
  private val jacksonFactory = new JacksonFactory()

  private val credentials: GoogleCredential = new GoogleCredential.Builder()
    .setTransport(httpTransport)
    .setJsonFactory(jacksonFactory)
    .setClientSecrets(clientId, clientSecret)
    .build
    .setRefreshToken(refreshToken)

  private val youTubeRequestLogger = new YouTubeRequestInitializer{
    override def initializeYouTubeRequest(request: YouTubeRequest[_]): Unit = {
      super.initializeYouTubeRequest(request)
      val markers: LogstashMarker = Markers.appendEntries(Map(
        "uri" -> request.getUriTemplate,
        "content" -> request.getHttpContent,
        "method" -> request.getRequestMethod
      ).asJava)
      log.info(markers, "YouTube Client Request")
    }
  }

  private val partnerCredentials = credentials.createScoped(List(YouTubeScopes.YOUTUBEPARTNER))

  // lazy to avoid initialising when in test
  lazy val client: YouTubeClient = new YouTubeClient.Builder(httpTransport, jacksonFactory, credentials)
    .setYouTubeRequestInitializer(youTubeRequestLogger)
    .setApplicationName(appName)
    .build

  lazy val partnerClient: YouTubePartner = new YouTubePartner.Builder(httpTransport, jacksonFactory, partnerCredentials)
    .setApplicationName(appName)
    .build()

  def categories: List[YouTubeVideoCategory] = {
    val request = client.videoCategories()
      .list("snippet")
      .setRegionCode("GB")

    YoutubeRequestLogger.logRequest(YoutubeApiType.DataApi, YoutubeRequestType.ListCategories)
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

    YoutubeRequestLogger.logRequest(YoutubeApiType.DataApi, YoutubeRequestType.ListChannels)
    request.execute().getItems.asScala.toList
      .map(YouTubeChannel.build(this, _))
  }

  def channelsWithData(hasMakePublicPermission: Boolean): List[YouTubeChannelWithData] = {
    channels.map(channel => YouTubeChannelWithData.build(this, channel.id, channel.title, hasMakePublicPermission))
      .sortBy(_.title)
  }

  def accessToken(): String = {
    credentials.refreshToken()
    credentials.getAccessToken
  }
}
