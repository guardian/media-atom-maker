package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class DimensionsToTranscode(
    height: Option[Int] = None,
    width: Option[Int] = None
)

sealed abstract class VideoInput {
  def id: String
}

case class YouTubeInput(id: String) extends VideoInput
case class SelfHostedInput(
    id: String,
    mimeType: String,
    dimensionsToTranscode: List[DimensionsToTranscode] = Nil
) extends VideoInput

object VideoInput {
  implicit val formatDimensionsToTranscode: Format[DimensionsToTranscode] =
    Jsonx.formatCaseClass[DimensionsToTranscode]
  implicit val formatYouTube: Format[YouTubeInput] =
    Jsonx.formatCaseClass[YouTubeInput]
  implicit val formatSelfHosted: Format[SelfHostedInput] =
    Jsonx.formatCaseClass[SelfHostedInput]
  implicit val format: Format[VideoInput] = Jsonx.formatSealed[VideoInput]

  def filename(input: VideoInput): String = input.id.split("/").last
  val mimeTypeMp4 = "video/mp4"
  val mimeTypeM3u8 = "application/vnd.apple.mpegurl"
  val mimeTypeVtt = "text/vtt"
  val captionsSuffix = "captions_00001.vtt"
  val firstFrameImageSuffix = ".0000000.jpg"
}
