package util

import com.gu.media.logging.Logging
import com.gu.media.model.PrivacyStatus
import com.gu.media.youtube._
import com.typesafe.config.Config
import play.api.cache.CacheApi

import scala.concurrent.duration.FiniteDuration

trait YouTube extends Logging with YouTubeAccess with YouTubeVideos with YouTubeClaims {
  val cache: CacheApi
  val duration: FiniteDuration

  override def categories: List[YouTubeVideoCategory] = {
    cache.getOrElse("categories", duration) { super.categories }
  }

  override def channels: List[YouTubeChannel] = {
    cache.getOrElse("channels", duration) { super.channels }
  }

  def getCommercialVideoInfo(videoId: String) = {
    getVideo(videoId, "snippet,contentDetails,status").map(video => {
      val channelId = video.getSnippet.getChannelId
      val channelTitle = video.getSnippet.getChannelTitle
      val channel = YouTubeChannel.build(this, channelId, channelTitle)

      val advertisingOptions = getAdvertisingOptions(videoId)
      YouTubeVideoCommercialInfo.build(video, advertisingOptions, channel)
    })
  }
}

object YouTube {
  def apply(_config: Config, _cache: CacheApi, _duration: FiniteDuration): YouTube = new YouTube {
    override def config: Config = _config
    override val cache: CacheApi = _cache
    override val duration: FiniteDuration = _duration
  }
}
