package com.gu.media.upload.mediaconvert.hls

import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.{
  CaptionsCodecWrapper,
  OutputDefinition,
  SelectorNames
}
import software.amazon.awssdk.services.mediaconvert.model._

object CaptionsOutput {
  def apply(): OutputDefinition = OutputDefinition(
    mimeType = Some(VideoSource.mimeTypeM3u8),
    assetType =
      None, // Not currently used as an asset, as the m3u8 playlist which combines this and video is used instead
    codec = CaptionsCodecWrapper(CaptionDestinationType.WEBVTT),
    output = () =>
      Output
        .builder()
        .containerSettings(
          ContainerSettings
            .builder()
            .container(ContainerType.CMFC)
            .cmfcSettings(CmfcSettings.builder().build())
            .build()
        )
        .nameModifier("_captions")
        .captionDescriptions(
          CaptionDescription
            .builder()
            .languageCode(LanguageCode.ENG)
            .captionSelectorName(SelectorNames.captions)
            .destinationSettings(
              CaptionDestinationSettings
                .builder()
                .destinationType(CaptionDestinationType.WEBVTT)
                .webvttDestinationSettings(
                  WebvttDestinationSettings.builder().build()
                )
                .build()
            )
            .build()
        )
        .build()
  )
}
