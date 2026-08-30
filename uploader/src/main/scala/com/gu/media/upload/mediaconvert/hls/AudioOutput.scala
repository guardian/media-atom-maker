package com.gu.media.upload.mediaconvert.hls

import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.{AudioCodecWrapper, OutputDefinition}
import com.gu.media.upload.mediaconvert.SharedCodecSettings.aacAudioDescription
import software.amazon.awssdk.services.mediaconvert.model._

object AudioOutput {
  def apply(): OutputDefinition =
    OutputDefinition(
      mimeType = None,
      assetType =
        None, // Not currently used as an asset, as the m3u8 playlist is used instead
      codec = AudioCodecWrapper(aacAudioDescription.codecSettings().codec()),
      output = () => {
        Output
          .builder()
          .containerSettings(
            ContainerSettings
              .builder()
              .container(ContainerType.CMFC)
              .cmfcSettings(CmfcSettings.builder().build())
              .build()
          )
          .nameModifier("_audio")
          .audioDescriptions(aacAudioDescription)
          .build()
      }
    )

}
