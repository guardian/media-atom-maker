package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.{Format, JsObject, JsString, Reads, Writes}

case class DimensionsToTranscode(
    height: Option[Int] = None,
    width: Option[Int] = None
)

sealed abstract class VideoInput {
  def id: String
  def platform: Platform
}

case class YouTubeInput(id: String, platform: Platform) extends VideoInput
case class SelfHostedInput(
    id: String,
    platform: Platform,
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

  implicit val videoInputWrites: Writes[VideoInput] = Writes {
    case y: YouTubeInput => formatYouTube.writes(y).as[JsObject]
    case s: SelfHostedInput => formatSelfHosted.writes(s).as[JsObject]
  }

  // I've added a Platform field here to derive the correct reads - could this be handled differently?
  implicit val videoInputReads: Reads[VideoInput] = Reads { json =>
    (json \ "platform").validate[Platform].flatMap {
      case platform if platform.toString.toLowerCase == "youtube" => formatYouTube.reads(json)
      case platform if platform.toString.toLowerCase == "url" => formatSelfHosted.reads(json)
      case other => play.api.libs.json.JsError(s"Unknown video input type: $other")
    }
  }

  implicit val format: Format[VideoInput] = Format(videoInputReads, videoInputWrites)

  def filename(input: VideoInput): String = input.id.split("/").last
  val mimeTypeMp4 = "video/mp4"
  val mimeTypeM3u8 = "application/vnd.apple.mpegurl"
  val mimeTypeVtt = "text/vtt"
  val captionsSuffix = "captions_00001.vtt"
  val firstFrameImageSuffix = ".0000000.jpg"
}
