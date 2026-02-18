package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class DimensionsToTranscode(
    height: Option[Int] = None,
    width: Option[Int] = None
)

case class VideoSource(
    src: String,
    mimeType: String,
    dimensionsToTranscode: List[DimensionsToTranscode]
)

sealed abstract class VideoInput
case class YouTubeInput(id: String) extends VideoInput
case class SelfHostedInput(sources: List[VideoSource]) extends VideoInput

object VideoSource {
  implicit val formatDimensionsToTranscode: Format[DimensionsToTranscode] = Jsonx.formatCaseClass[DimensionsToTranscode]
  implicit val format: Format[VideoSource] = Jsonx.formatCaseClass[VideoSource]
  def filename(source: VideoSource): String = source.src.split("/").last
  val mimeTypeMp4 = "video/mp4"
  val mimeTypeM3u8 = "application/vnd.apple.mpegurl"
  val mimeTypeVtt = "text/vtt"
  val captionsSuffix = "captions_00001.vtt"
  val firstFrameImageSuffix = ".0000000.jpg"
}

object VideoInput {
  implicit val formatYouTube: Format[YouTubeInput] =
    Jsonx.formatCaseClass[YouTubeInput]
  implicit val formatSelfHosted: Format[SelfHostedInput] =
    Jsonx.formatCaseClass[SelfHostedInput]
  implicit val format: Format[VideoInput] = Jsonx.formatSealed[VideoInput]
}
