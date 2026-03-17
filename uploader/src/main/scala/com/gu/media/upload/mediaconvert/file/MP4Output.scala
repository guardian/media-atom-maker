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

case class Dimensions(width: Option[Int], height: Option[Int])

case class ResolutionConfig(
    dimensions: Dimensions,
    bitrate: BitrateSetting,
    nameModifier: String
)

object MP4Output {
  val highRes: ResolutionConfig =
    ResolutionConfig(Dimensions(None, Some(720)), highBitrate, "_720h")
  val lowRes: ResolutionConfig =
    ResolutionConfig(Dimensions(Some(480), None), lowBitrate, "_480w")

  def apply(config: ResolutionConfig): OutputDefinition = {
    OutputDefinition(
      mimeType = Some(VideoSource.mimeTypeMp4),
      assetType = Some(AssetType.Video),
      output = () =>
        Output
          .builder()
          .nameModifier(config.nameModifier)
          .containerSettings(
            ContainerSettings
              .builder()
              .container(ContainerType.MP4)
              .mp4Settings(Mp4Settings.builder().build())
              .build()
          )
          .videoDescription(
            VideoDescription
              .builder()
              .height(config.dimensions.height.map(Integer.valueOf).orNull)
              .width(config.dimensions.width.map(Integer.valueOf).orNull)
              .sharpness(100) // Sharpest possible
              .codecSettings(
                VideoCodecSettings
                  .builder()
                  .codec(VideoCodec.H_264)
                  .h264Settings(h264Settings(config.bitrate))
                  .build()
              )
              .build()
          )
          .audioDescriptions(aacAudioDescription)
          .build()
    )
  }
}
