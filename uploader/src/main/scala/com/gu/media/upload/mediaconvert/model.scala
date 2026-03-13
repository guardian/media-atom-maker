package com.gu.media.upload.mediaconvert

import com.gu.contentatom.thrift.atom.media.AssetType
import software.amazon.awssdk.services.mediaconvert.model.{Output, OutputGroup}

case class OutputDefinition(
    output: () => Output,
    mimeType: Option[String],
    assetType: Option[AssetType]
)

case class OutputGroupDefinition(
    outputGroup: String => OutputGroup,
    outputs: List[OutputDefinition],
    mimeType: Option[String],
    assetType: Option[AssetType]
)
