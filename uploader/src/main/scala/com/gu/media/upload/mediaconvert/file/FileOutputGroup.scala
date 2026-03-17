package com.gu.media.upload.mediaconvert.file

import com.gu.media.upload.mediaconvert.OutputGroupDefinition
import com.gu.media.upload.mediaconvert.file.MP4Output.{highRes, lowRes}
import software.amazon.awssdk.services.mediaconvert.model.{FileGroupSettings, OutputGroup, OutputGroupSettings, OutputGroupType}

object FileOutputGroup {
  def apply(): OutputGroupDefinition = {
    val outputs =
      List(MP4Output(lowRes), MP4Output(highRes), JPEGOutput(), WebVTTOutput())
    OutputGroupDefinition(
      mimeType = None,
      assetType =
        None, // Individual outputs are used as assets, but the group itself isn't
      outputs = outputs,
      outputGroup = (destination: String) =>
        OutputGroup
          .builder()
          .name("MP4")
          .outputs(outputs.map(_.output()): _*)
          .outputGroupSettings(
            OutputGroupSettings
              .builder()
              .`type`(OutputGroupType.FILE_GROUP_SETTINGS)
              .fileGroupSettings(
                FileGroupSettings
                  .builder()
                  .destination(destination)
                  .build()
              )
              .build()
          )
          .build()
    )
  }
}
