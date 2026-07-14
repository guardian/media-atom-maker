package com.gu.media.upload.mediaconvert.hls

import com.gu.contentatom.thrift.atom.media.AssetType
import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.{
  AV1EncodingConfig,
  Dimensions,
  EncodingConfig,
  H264EncodingConfig,
  OutputDefinition,
  OutputGroupDefinition,
  VideoCodecWrapper
}
import software.amazon.awssdk.services.mediaconvert.model.{
  CmafAdditionalManifest,
  CmafGroupSettings,
  CmafWriteDASHManifest,
  OutputGroup,
  OutputGroupSettings,
  OutputGroupType,
  VideoCodec
}

import scala.jdk.CollectionConverters._

object HLSOutputGroup {

  private val commonDimensions = List(
    Dimensions(Some(480), None),
    Dimensions(None, Some(720))
  )

  private val outputDefinitions: List[
    ((Dimensions, Int) => EncodingConfig, List[Dimensions], List[Int])
  ] = List(
    (H264EncodingConfig, commonDimensions, List(8, 4)),
    (AV1EncodingConfig, commonDimensions, List(8, 4))
  )

  private val videoOutputs = for {
    (encodingConfig, dimensions, qualities) <- outputDefinitions
    dimension <- dimensions
    quality <- qualities
  } yield VideoOutput(encodingConfig(dimension, quality))

  def manifestWithoutCodec(
      outputs: List[OutputDefinition],
      codec: VideoCodec,
      nameModifier: String
  ): CmafAdditionalManifest = {
    CmafAdditionalManifest
      .builder()
      .manifestNameModifier(nameModifier)
      .selectedOutputs(
        outputs
          .filter(o => o.codec != VideoCodecWrapper(codec))
          .map(_.output().nameModifier())
          .asJava
      )
      .build()
  }

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
                  .additionalManifests(
                    List(
                      manifestWithoutCodec(outputs, VideoCodec.H_264, "_av1"),
                      manifestWithoutCodec(outputs, VideoCodec.AV1, "_h264")
                    ).asJava
                  )
                  .build()
              )
              .build()
          )
          .build()
    )
  }
}
