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

sealed trait ResolutionConfig {
  def dimensions: Dimensions
  def bitrate: BitrateSetting
  def nameModifier: String
}

object Resolution {
  case object High extends ResolutionConfig {
    val dimensions = Dimensions(None, Some(720))
    val bitrate = highBitrate
    val nameModifier = "_720h"
  }

  case object Low extends ResolutionConfig {
    val dimensions = Dimensions(Some(480), None)
    val bitrate = lowBitrate
    val nameModifier = "_480w"
  }
}

object MP4Output {

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
