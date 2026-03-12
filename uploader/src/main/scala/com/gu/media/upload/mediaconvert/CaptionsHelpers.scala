package com.gu.media.upload.mediaconvert

import software.amazon.awssdk.services.mediaconvert.model.{
  CaptionSelector,
  CaptionSourceSettings,
  FileSourceSettings
}

import scala.jdk.CollectionConverters._

object CaptionsHelpers {
  private def captionsSource(subtitlesInput: String) = {
    CaptionSourceSettings
      .builder()
      .sourceType("SRT")
      .fileSourceSettings(
        FileSourceSettings
          .builder()
          .sourceFile(subtitlesInput)
          .build()
      )
      .build()
  }

  private val nullCaptionsSource =
    CaptionSourceSettings.builder().sourceType("NULL_SOURCE").build()

  def captionSelectors(subtitlesInput: Option[String]) = {
    val captionSourceSettings =
      subtitlesInput.map(captionsSource).getOrElse(nullCaptionsSource)

    Map(
      SelectorNames.captions -> CaptionSelector
        .builder()
        .sourceSettings(
          captionSourceSettings
        )
        .build()
    ).asJava
  }
}
