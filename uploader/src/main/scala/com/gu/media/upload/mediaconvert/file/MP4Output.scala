package com.gu.media.upload.mediaconvert.file

import com.gu.contentatom.thrift.atom.media.AssetType
import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.{
  EncodingConfig,
  OutputDefinition,
  VideoCodecWrapper
}
import com.gu.media.upload.mediaconvert.SharedCodecSettings.aacAudioDescription
import software.amazon.awssdk.services.mediaconvert.model._

object MP4Output {

  def apply(config: EncodingConfig, hasAudio: Boolean): OutputDefinition = {
    OutputDefinition(
      mimeType = Some(VideoSource.mimeTypeMp4),
      assetType = Some(AssetType.Video),
      output = () => {
        val outputBuilder = Output
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
                config.codecSettings
              )
              .build()
          )

        if (hasAudio)
          outputBuilder.audioDescriptions(aacAudioDescription).build()
        else outputBuilder.build()
      },
      codec = VideoCodecWrapper(config.codecSettings.codec())
    )
  }
}
