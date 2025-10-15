package util

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.gu.media.logging.Logging
import com.gu.media.youtube._
import com.typesafe.config.Config

import java.time.Duration

trait YouTube
    extends Logging
    with YouTubeAccess
    with YouTubeVideos
    with YouTubePartnerApi {
  val duration: Duration

  private lazy val categoriesCache = Memoize(super.categories, duration)

  private lazy val channelsCache = Memoize(super.channels, duration)

  override def categories: List[YouTubeVideoCategory] =
    categoriesCache.get

  override def channels: List[YouTubeChannel] =
    channelsCache.get

  def getCommercialVideoInfo(videoId: String) = {
    getVideo(videoId, List("snippet", "contentDetails", "status")).map(
      video => {
        val channelId = video.getSnippet.getChannelId
        val channelTitle = video.getSnippet.getChannelTitle
        val channel = YouTubeChannel.build(this, channelId, channelTitle)

        val advertisingOptions = getAdvertisingOptions(videoId)
        YouTubeVideoCommercialInfo.build(video, advertisingOptions, channel)
      }
    )
  }
}

object YouTube {

  def apply(
      _config: Config,
      _duration: Duration,
      _credentials: GoogleCredential
  ): YouTube = {
    new YouTube {
      override def youtubeCredentials: GoogleCredential = _credentials
      override def config: Config = _config
      override val duration: Duration = _duration
    }
  }
}
