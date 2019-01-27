package com.gu.media.youtube

object YoutubeUrl {
  private val re = "^https://(?:www.youtube.com/(?:watch\\?v=|embed/)|youtu.be/)([a-zA-Z0-9_-]{11}).*$".r

  def unapply(url: String): Option[String] = parse(url)

  def parse(url: String): Option[String] = {
    url match {
      case re(videoId) => Some(videoId)
      case _ => None
    }
  }
}
