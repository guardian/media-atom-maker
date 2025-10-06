package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class VideoSource(src: String, mimeType: String)

sealed abstract class VideoAsset
case class YouTubeAsset(id: String) extends VideoAsset
case class SelfHostedAsset(sources: List[VideoSource]) extends VideoAsset

object VideoSource {
  implicit val format: Format[VideoSource] = Jsonx.formatCaseClass[VideoSource]
  def filename(source: VideoSource): String = source.src.split("/").last
  val mimeTypeMp4 = "video/mp4"
  val mimeTypeM3u8 = "application/vnd.apple.mpegurl"
  val mimeTypeVtt = "text/vtt"
  val captionsSuffix = "captions_00001.vtt"
  val firstFrameImageSuffix = ".0000000.jpg"
}

object VideoAsset {
  implicit val formatYouTube: Format[YouTubeAsset] = Jsonx.formatCaseClass[YouTubeAsset]
  implicit val formatSelfHosted: Format[SelfHostedAsset] = Jsonx.formatCaseClass[SelfHostedAsset]
  implicit val format: Format[VideoAsset] = Jsonx.formatSealed[VideoAsset]
}
