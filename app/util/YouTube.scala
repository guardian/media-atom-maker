package util

import com.gu.media.logging.Logging
import com.gu.media.youtube.{YouTubeAccess, YouTubeChannel, YouTubeVideoCategory, YouTubeVideos}
import com.typesafe.config.Config
import play.api.cache.CacheApi

import scala.concurrent.duration.FiniteDuration

trait YouTube extends Logging with YouTubeAccess with YouTubeVideos {
  val cache: CacheApi
  val duration: FiniteDuration

  override def categories: List[YouTubeVideoCategory] = {
    cache.getOrElse("categories", duration) { super.categories }
  }

  override def channels: List[YouTubeChannel] = {
    cache.getOrElse("channels", duration) { super.channels }
  }
}

object YouTube {
  def apply(_config: Config, _cache: CacheApi, _duration: FiniteDuration): YouTube = new YouTube {
    override def config: Config = _config
    override val cache: CacheApi = _cache
    override val duration: FiniteDuration = _duration
  }
}
