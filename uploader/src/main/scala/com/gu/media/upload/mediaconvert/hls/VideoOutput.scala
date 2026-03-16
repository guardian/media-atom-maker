package com.gu.media.upload.mediaconvert.hls

import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.OutputDefinition
import com.gu.media.upload.mediaconvert.SharedCodecSettings.{aacAudioDescription, h264Settings, highBitrate}
import software.amazon.awssdk.services.mediaconvert.model._

object VideoOutput {
  def apply(): OutputDefinition = OutputDefinition(
    mimeType = Some(VideoSource.mimeTypeM3u8),
    assetType =
      None, // Not currently used as an asset, as the m3u8 playlist which combines this and subtitles is used instead
    output = () =>
      Output
        .builder()
        .containerSettings(
          ContainerSettings
            .builder()
            .container(ContainerType.M3_U8)
            .m3u8Settings(M3u8Settings.builder().build())
            .build()
        )
        .nameModifier("hls")
        .videoDescription(
          VideoDescription
            .builder()
            .height(720)
            .sharpness(100) // Sharpest possible
            .videoPreprocessors(
              VideoPreprocessor
                .builder()
                .colorCorrector( // NOTE: only needed for m3u8
                  ColorCorrector
                    .builder()
                    .colorSpaceConversion(
                      ColorSpaceConversion.FORCE_709
                    ) // Convert to Rec.709 SDR colourspace as per HLS spec pt. 1.24
                    .build()
                )
                .build()
            )
            .codecSettings(
              VideoCodecSettings
                .builder()
                .codec(VideoCodec.H_264)
                .h264Settings(h264Settings(highBitrate))
                .build()
            )
            .build()
        )
        .audioDescriptions(aacAudioDescription)
        .build()
  )

}
