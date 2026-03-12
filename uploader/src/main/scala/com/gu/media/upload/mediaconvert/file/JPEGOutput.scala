package com.gu.media.upload.mediaconvert.file

import com.gu.media.model.VideoSource
import com.gu.media.upload.mediaconvert.OutputDefinition
import software.amazon.awssdk.services.mediaconvert.model._

object JPEGOutput {
  def apply(): OutputDefinition = OutputDefinition(
    mimeType = Some(VideoSource.mimeTypeJpeg),
    assetType = None, // Not currently used as an asset
    output = () =>
      Output
        .builder()
        .containerSettings(
          ContainerSettings
            .builder()
            .container(ContainerType.RAW)
            .build()
        )
        .videoDescription(
          VideoDescription
            .builder()
            .codecSettings(
              VideoCodecSettings
                .builder()
                .codec(VideoCodec.FRAME_CAPTURE)
                .frameCaptureSettings(
                  FrameCaptureSettings
                    .builder()
                    .maxCaptures(1)
                    .quality(95)
                    .build()
                )
                .build()
            )
            .build()
        )
        .build()
  )
}
