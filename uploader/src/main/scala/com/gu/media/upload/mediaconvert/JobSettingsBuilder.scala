package com.gu.media.upload.mediaconvert

import com.gu.media.upload.mediaconvert.file.FileOutputGroup
import com.gu.media.upload.mediaconvert.hls.HLSOutputGroup
import software.amazon.awssdk.services.mediaconvert.model._
import CaptionsHelpers.captionSelectors

import scala.jdk.CollectionConverters._

// If this definition changes, care needs to be taken to ensure that a pipeline isn't part way through
// processing with the old definition, otherwise the output assets may not be correctly created and linked to the atom

object SelectorNames {
  val audio = "Dynamic Audio Selector 1"
  val captions = "Caption Selector 1"
}

object JobSettingsBuilder {

  val outputGroups: List[OutputGroupDefinition] =
    List(FileOutputGroup(), HLSOutputGroup())

  def build(
      videoInput: String,
      subtitlesInput: Option[String],
      destination: String
  ): JobSettings = {

    val dynamicAudioSelectors = Map(
      SelectorNames.audio -> DynamicAudioSelector
        .builder()
        .build()
    ).asJava

    val jobInput = Input
      .builder()
      .fileInput(videoInput)
      .dynamicAudioSelectors(dynamicAudioSelectors)
      .captionSelectors(captionSelectors(subtitlesInput))
      .timecodeSource(InputTimecodeSource.ZEROBASED)
      .build()

    val timecodeConfig = TimecodeConfig
      .builder()
      .source(TimecodeSource.ZEROBASED)
      .build()

    JobSettings
      .builder()
      .timecodeConfig(timecodeConfig)
      .inputs(jobInput)
      .outputGroups(
        outputGroups.map(_.outputGroup(destination)): _*
      )
      .followSource(1)
      .build()
  }
}
