package com.gu.media.upload.mediaconvert.file

import com.gu.contentatom.thrift.atom.media.AssetType
import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.{
  CaptionsCodecWrapper,
  OutputDefinition,
  SelectorNames
}
import software.amazon.awssdk.services.mediaconvert.model._

object WebVTTOutput {
  def apply(): OutputDefinition = OutputDefinition(
    mimeType = Some(VideoSource.mimeTypeVtt),
    assetType = Some(AssetType.Subtitles),
    output = () =>
      Output
        .builder()
        .containerSettings(
          ContainerSettings
            .builder()
            .container(ContainerType.RAW)
            .build()
        )
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
        .extension("vtt") // explicitly setting the extension avoids an AWS bug where the extension isn't included in the media convert complete event
        .build(),
    codec = CaptionsCodecWrapper(CaptionDestinationType.WEBVTT)
  )
}
