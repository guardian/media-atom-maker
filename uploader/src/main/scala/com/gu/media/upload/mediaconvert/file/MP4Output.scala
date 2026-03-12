package com.gu.media.upload.mediaconvert.file

import com.gu.contentatom.thrift.atom.media.AssetType
import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.OutputDefinition
import com.gu.media.upload.mediaconvert.SharedCodecSettings.{
  aacAudioDescription,
  h264Settings
}
import software.amazon.awssdk.services.mediaconvert.model._

object MP4Output {
  def apply(): OutputDefinition = OutputDefinition(
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
          VideoDescription
            .builder()
            .height(720)
            .sharpness(100) // Sharpest possible
            .codecSettings(
              VideoCodecSettings
                .builder()
                .codec(VideoCodec.H_264)
                .h264Settings(h264Settings)
                .build()
            )
            .build()
        )
        .audioDescriptions(aacAudioDescription)
        .build()
  )
}
