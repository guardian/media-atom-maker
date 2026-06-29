package com.gu.media.upload.mediaconvert.hls

import com.gu.contentatom.thrift.atom.media.AssetType
import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.{EncodingConfigs, OutputGroupDefinition}
import software.amazon.awssdk.services.mediaconvert.model.{
  CmafGroupSettings,
  CmafWriteDASHManifest,
  OutputGroup,
  OutputGroupSettings,
  OutputGroupType
}

object HLSOutputGroup {
  def apply(hasAudio: Boolean): OutputGroupDefinition = {
    val outputs = List(
      VideoOutput(EncodingConfigs.MobileWidth),
      VideoOutput(EncodingConfigs.Default),
      VideoOutput(EncodingConfigs.LowQuality),
      VideoOutput(EncodingConfigs.LowQualityMobileWidth),
      VideoOutput(EncodingConfigs.VeryLowQuality),
      VideoOutput(EncodingConfigs.VeryLowQualityMobileWidth),
      CaptionsOutput()
    ) ++ (if (hasAudio) List(AudioOutput()) else Nil)
    OutputGroupDefinition(
      mimeType = Some(VideoSource.mimeTypeM3u8),
      assetType = Some(
        AssetType.Video
      ), // The m3u8 playlist which combines video and captions is used as the asset
      outputs = outputs,
      outputGroup = (destination: String) =>
        OutputGroup
          .builder()
          .name("CMAF HLS")
          .outputs(outputs.map(_.output()): _*)
          .outputGroupSettings(
            OutputGroupSettings
              .builder()
              .`type`(OutputGroupType.CMAF_GROUP_SETTINGS)
              .cmafGroupSettings(
                CmafGroupSettings
                  .builder()
                  .writeDashManifest(CmafWriteDASHManifest.DISABLED)
                  .segmentLength(10)
                  .fragmentLength(2)
                  .destination(destination)
                  .build()
              )
              .build()
          )
          .build()
    )
  }
}
