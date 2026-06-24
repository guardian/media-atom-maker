package com.gu.media.upload.mediaconvert

import com.gu.media.upload.mediaconvert.SharedCodecSettings.{
  av1Settings,
  h264Settings
}
import software.amazon.awssdk.services.mediaconvert.model.{
  VideoCodec,
  VideoCodecSettings
}

case class Dimensions(width: Option[Int], height: Option[Int])

sealed trait EncodingConfig {
  def dimensions: Dimensions
  def nameModifier: String = {
    val dimensionPart = List(
      dimensions.width.map(w => s"${w}w"),
      dimensions.height.map(h => s"${h}h")
    ).flatten.mkString("_")

    s"_${dimensionPart}_q${qualityLevel}_${codecSettings.codecAsString.replace("_", "").toLowerCase}"
  }
  def qualityLevel: Int
  def codecSettings: VideoCodecSettings
}

case class H264EncodingConfig(
    dimensions: Dimensions,
    qualityLevel: Int
) extends EncodingConfig {
  override def codecSettings: VideoCodecSettings = VideoCodecSettings
    .builder()
    .codec(VideoCodec.H_264)
    .h264Settings(h264Settings(qualityLevel))
    .build()
}

case class AV1EncodingConfig(
    dimensions: Dimensions,
    qualityLevel: Int
) extends EncodingConfig {
  override def codecSettings: VideoCodecSettings = VideoCodecSettings
    .builder()
    .codec(VideoCodec.AV1)
    .av1Settings(av1Settings(qualityLevel))
    .build()
}

object EncodingConfigs {
  val Default = H264EncodingConfig(Dimensions(None, Some(720)), 8)

  val MobileWidth = H264EncodingConfig(Dimensions(Some(480), None), 8)
}
