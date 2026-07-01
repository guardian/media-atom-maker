package com.gu.media.upload.mediaconvert.hls

import com.gu.contentatom.thrift.atom.media.AssetType
import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.{
  Dimensions,
  EncodingConfig,
  H264EncodingConfig,
  OutputGroupDefinition
}
import software.amazon.awssdk.services.mediaconvert.model.{
  CmafGroupSettings,
  CmafWriteDASHManifest,
  OutputGroup,
  OutputGroupSettings,
  OutputGroupType
}

object HLSOutputGroup {

  private val commonDimensions = List(
    Dimensions(Some(480), None),
    Dimensions(None, Some(720))
  )

  private val outputDefinitions: List[
    ((Dimensions, Int) => EncodingConfig, List[Dimensions], List[Int])
  ] = List(
    (H264EncodingConfig, commonDimensions, List(8, 6, 4))
  )

  private val videoOutputs = for {
    (encodingConfig, dimensions, qualities) <- outputDefinitions
    dimension <- dimensions
    quality <- qualities
  } yield VideoOutput(encodingConfig(dimension, quality))

  def apply(hasAudio: Boolean): OutputGroupDefinition = {
    val outputs = videoOutputs ++
      List(CaptionsOutput()) ++
      List(AudioOutput()).filter(_ => hasAudio)

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
