package com.gu.media.upload.mediaconvert.file

import com.gu.contentatom.thrift.atom.media.AssetType
import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.{BitrateSetting, OutputDefinition}
import com.gu.media.upload.mediaconvert.SharedCodecSettings.{
  aacAudioDescription,
  h264Settings,
  highBitrate,
  lowBitrate
}
import software.amazon.awssdk.services.mediaconvert.model._

sealed trait Resolution
case object HighRes extends Resolution
case object LowRes extends Resolution

sealed trait Dimension {
  def apply(
      builder: VideoDescription.Builder,
      value: Int
  ): VideoDescription.Builder
}

case object Height extends Dimension {
  def apply(builder: VideoDescription.Builder, value: Int) =
    builder.height(value)
}

case object Width extends Dimension {
  def apply(builder: VideoDescription.Builder, value: Int) =
    builder.width(value)
}

case class ResolutionConfig(
    dimension: Dimension,
    dimensionValue: Int,
    bitrate: BitrateSetting
)

object MP4Output {

  def apply(resolution: Resolution): OutputDefinition = {
    val config = resolution match {
      case HighRes =>
        ResolutionConfig(Height, 720, highBitrate)
      case LowRes =>
        ResolutionConfig(Width, 480, lowBitrate)
    }

    OutputDefinition(
      mimeType = Some(VideoSource.mimeTypeMp4),
      assetType = Some(AssetType.Video),
      output = () =>
        Output
          .builder()
          .containerSettings(
            ContainerSettings
              .builder()
              .container(ContainerType.MP4)
              .mp4Settings(Mp4Settings.builder().build())
              .build()
          )
          .videoDescription(
            config
              .dimension(
                VideoDescription
                  .builder()
                  .sharpness(100) // Sharpest possible
                  .codecSettings(
                    VideoCodecSettings
                      .builder()
                      .codec(VideoCodec.H_264)
                      .h264Settings(h264Settings(config.bitrate))
                      .build()
                  ),
                config.dimensionValue
              )
              .build()
          )
          .audioDescriptions(aacAudioDescription)
          .build()
    )
  }
}
