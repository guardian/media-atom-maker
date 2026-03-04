package com.gu.media.upload

import software.amazon.awssdk.services.mediaconvert.model._
import scala.jdk.CollectionConverters._

object JobSettingsBuilder {
  private val h264Settings: H264Settings =
    H264Settings
      .builder()
      .rateControlMode(H264RateControlMode.QVBR) // Best quality
      .qualityTuningLevel(H264QualityTuningLevel.MULTI_PASS_HQ) // Best quality
      .maxBitrate(4_800_000) // Migrated from Elastic Transcoder
      .qvbrSettings(
        H264QvbrSettings
          .builder()
          .maxAverageBitrate(
            2_400_000
          ) // Twice average to give room for encoder's decisions
          .qvbrQualityLevel(8) // 1-10, 10 is best quality
          .qvbrQualityLevelFineTune(0.66) // Fine-tuned through manual testing
          .build()
      )
      .framerateControl(
        H264FramerateControl.INITIALIZE_FROM_SOURCE
      ) // Follow the source frame rate (default)
      .parControl(
        H264ParControl.SPECIFIED
      ) // Use the pixel aspect ratio specified below
      .parNumerator(1) // 1:1 pixel aspect ratio
      .parDenominator(1) // 1:1 pixel aspect ratio
      .hrdBufferSize(
        10_000_000
      ) // HrdBufferSize / Bitrate gives a buffer duration of 2-4 seconds
      .hrdBufferInitialFillPercentage(90) // Default
      .gopSizeUnits(H264GopSizeUnits.SECONDS)
      .gopSize(2.0) // 2 seconds
      .numberBFramesBetweenReferenceFrames(
        1
      ) // 0-15, smaller number gives better quality
      .sceneChangeDetect(
        H264SceneChangeDetect.DISABLED
      ) // Reduce interference for videos which loop back to the start
      .codecProfile(
        H264CodecProfile.HIGH
      ) // Good support for hardware decoding; required by HLS
      .codecLevel(H264CodecLevel.AUTO) // Recommended by HLS, effectively 3.1
      .entropyEncoding(H264EntropyEncoding.CABAC) // Best quality
      .slices(1) // Slices improve encoding speed at the expense of quality
      .saliencyAwareEncoding(
        H264SaliencyAwareEncoding.PREFERRED
      ) // Best quality
      // BandwidthReductionFilter left unset — having it on reduces detail
      .adaptiveQuantization(H264AdaptiveQuantization.HIGH) // Best quality
      .spatialAdaptiveQuantization(
        H264SpatialAdaptiveQuantization.ENABLED
      ) // Best quality
      .temporalAdaptiveQuantization(
        H264TemporalAdaptiveQuantization.ENABLED
      ) // Best quality
      .flickerAdaptiveQuantization(
        H264FlickerAdaptiveQuantization.ENABLED
      ) // Best quality
      .build()

  private val aacAudioDescription: AudioDescription =
    AudioDescription
      .builder()
      .audioSourceName("Dynamic Audio Selector 1")
      .codecSettings(
        AudioCodecSettings
          .builder()
          .codec(AudioCodec.AAC)
          .aacSettings(
            AacSettings
              .builder()
              .bitrate(160_000)
              .codingMode(AacCodingMode.CODING_MODE_2_0)
              .sampleRate(44_100)
              .build()
          )
          .build()
      )
      .build()

  private val mp4VideoOutput: Output =
    Output
      .builder()
      .containerSettings(
        ContainerSettings
          .builder()
          .container(ContainerType.MP4)
          .mp4Settings(Mp4Settings.builder().build())
          .build()
      )
      .videoDescription(
        VideoDescription
          .builder()
          .height(720)
          .sharpness(100) // Sharpest possible
          .codecSettings(
            VideoCodecSettings
              .builder()
              .codec(VideoCodec.H_264)
              .h264Settings(h264Settings)
              .build()
          )
          .build()
      )
      .audioDescriptions(aacAudioDescription)
      .build()

  private val frameCaptureOutput: Output =
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

  private def mp4OutputGroup(destination: String): OutputGroup =
    OutputGroup
      .builder()
      .name("MP4")
      .outputs(mp4VideoOutput, frameCaptureOutput, vttCaptionsOutput)
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

  private val hlsVideoOutput: Output =
    Output
      .builder()
      .containerSettings(
        ContainerSettings
          .builder()
          .container(ContainerType.M3_U8)
          .m3u8Settings(M3u8Settings.builder().build())
          .build()
      )
      .nameModifier("hls")
      .videoDescription(
        VideoDescription
          .builder()
          .height(720)
          .sharpness(100) // Sharpest possible
          .videoPreprocessors(
            VideoPreprocessor
              .builder()
              .colorCorrector( // NOTE: only needed for m3u8
                ColorCorrector
                  .builder()
                  .colorSpaceConversion(
                    ColorSpaceConversion.FORCE_709
                  ) // Convert to Rec.709 SDR colourspace as per HLS spec pt. 1.24
                  .build()
              )
              .build()
          )
          .codecSettings(
            VideoCodecSettings
              .builder()
              .codec(VideoCodec.H_264)
              .h264Settings(h264Settings)
              .build()
          )
          .build()
      )
      .audioDescriptions(aacAudioDescription)
      .build()

  private val vttCaptionsOutput: Output =
    Output
      .builder()
      .containerSettings(
        ContainerSettings
          .builder()
          .container(ContainerType.RAW)
          .build()
      )
      .captionDescriptions(
        CaptionDescription
          .builder()
          .languageCode(LanguageCode.ENG)
          .captionSelectorName("Caption Selector 1")
          .destinationSettings(
            CaptionDestinationSettings
              .builder()
              .destinationType(CaptionDestinationType.WEBVTT)
              .webvttDestinationSettings(
                WebvttDestinationSettings.builder().build()
              )
              .build()
          )
          .build()
      )
      .build()

  private val hlsCaptionsOutput: Output =
    Output
      .builder()
      .containerSettings(
        ContainerSettings
          .builder()
          .container(ContainerType.M3_U8)
          .m3u8Settings(M3u8Settings.builder().build())
          .build()
      )
      .nameModifier("captions")
      .captionDescriptions(
        CaptionDescription
          .builder()
          .languageCode(LanguageCode.ENG)
          .captionSelectorName("Caption Selector 1")
          .destinationSettings(
            CaptionDestinationSettings
              .builder()
              .destinationType(CaptionDestinationType.WEBVTT)
              .webvttDestinationSettings(
                WebvttDestinationSettings.builder().build()
              )
              .build()
          )
          .build()
      )
      .build()

  private def hlsOutputGroup(destination: String) =
    OutputGroup
      .builder()
      .name("Apple HLS")
      .outputs(hlsVideoOutput, hlsCaptionsOutput)
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

  private def subtitlesSource(subtitlesInput: String) = {
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

  private val nullSubtitlesSource =
    CaptionSourceSettings.builder().sourceType("NULL_SOURCE").build()

  private val dynamicAudioSelectors = Map(
    "Dynamic Audio Selector 1" -> DynamicAudioSelector
      .builder()
      .build()
  ).asJava

  private def captionSelectors(captionSourceSettings: CaptionSourceSettings) = {
    Map(
      "Caption Selector 1" -> CaptionSelector
        .builder()
        .sourceSettings(
          captionSourceSettings
        )
        .build()
    ).asJava
  }

  def build(
      videoInput: String,
      subtitlesInput: Option[String],
      destination: String
  ): JobSettings = {
    val captionSourceSettings =
      subtitlesInput.map(subtitlesSource).getOrElse(nullSubtitlesSource)

    val jobInput = Input
      .builder()
      .fileInput(videoInput)
      .dynamicAudioSelectors(dynamicAudioSelectors)
      .captionSelectors(captionSelectors(captionSourceSettings))
      .timecodeSource(InputTimecodeSource.ZEROBASED)
      .build()

    JobSettings
      .builder()
      .timecodeConfig(
        TimecodeConfig
          .builder()
          .source(TimecodeSource.ZEROBASED)
          .build()
      )
      .inputs(jobInput)
      .outputGroups(mp4OutputGroup(destination), hlsOutputGroup(destination))
      .followSource(1)
      .build()
  }
}
