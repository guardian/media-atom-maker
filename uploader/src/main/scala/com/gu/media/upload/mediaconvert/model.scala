package com.gu.media.upload.mediaconvert

import com.gu.contentatom.thrift.atom.media.AssetType
import software.amazon.awssdk.services.mediaconvert.model.{
  AudioCodec,
  CaptionDestinationType,
  Output,
  OutputGroup,
  VideoCodec
}

sealed trait Codec

case class AudioCodecWrapper(codec: AudioCodec) extends Codec
case class VideoCodecWrapper(codec: VideoCodec) extends Codec
case class CaptionsCodecWrapper(codec: CaptionDestinationType) extends Codec

case class OutputDefinition(
    output: () => Output,
    codec: Codec,
    mimeType: Option[String],
    assetType: Option[AssetType]
)

case class OutputGroupDefinition(
    outputGroup: String => OutputGroup,
    outputs: List[OutputDefinition],
    mimeType: Option[String],
    assetType: Option[AssetType]
)
