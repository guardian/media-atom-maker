package com.gu.media.upload.mediaconvert.hls

import com.gu.contentatom.thrift.atom.media.AssetType
import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.OutputGroupDefinition
import software.amazon.awssdk.services.mediaconvert.model.{
  HlsGroupSettings,
  OutputGroup,
  OutputGroupSettings,
  OutputGroupType
}

object HLSOutputGroup {
  def apply(): OutputGroupDefinition = {
    val outputs = List(VideoOutput(), CaptionsOutput())
    OutputGroupDefinition(
      mimeType = Some(VideoSource.mimeTypeM3u8),
      assetType = Some(
        AssetType.Video
      ), // The m3u8 playlist which combines video and captions is used as the asset
      outputs = outputs,
      outputGroup = (destination: String) =>
        OutputGroup
          .builder()
          .name("Apple HLS")
          .outputs(outputs.map(_.output()): _*)
          .outputGroupSettings(
            OutputGroupSettings
              .builder()
              .`type`(OutputGroupType.HLS_GROUP_SETTINGS)
              .hlsGroupSettings(
                HlsGroupSettings
                  .builder()
                  .segmentLength(10)
                  .minSegmentLength(0)
                  .destination(destination)
                  .build()
              )
              .build()
          )
          .build()
    )
  }
}
