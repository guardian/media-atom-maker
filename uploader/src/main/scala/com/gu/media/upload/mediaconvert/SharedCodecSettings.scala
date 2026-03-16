package com.gu.media.upload.mediaconvert

import software.amazon.awssdk.services.mediaconvert.model._

case class BitrateSetting(max: Int, maxAverage: Int)

object SharedCodecSettings {

  /** Migrated from Elastic Transcoder */
  val highBitrate: BitrateSetting = BitrateSetting(4_800_000, 2_400_000);

  /** Used for Mobile videos at 480px width. @see
    * https://support.google.com/youtube/answer/2853702?hl=en-GB
    */
  val lowBitrate: BitrateSetting = BitrateSetting(2_400_000, 1_200_000)

  def h264Settings(Bitrate: BitrateSetting): H264Settings =
    H264Settings
      .builder()
      .rateControlMode(H264RateControlMode.QVBR) // Best quality
      .qualityTuningLevel(H264QualityTuningLevel.MULTI_PASS_HQ) // Best quality
      .maxBitrate(Bitrate.max) // Migrated from Elastic Transcoder
      .qvbrSettings(
        H264QvbrSettings
          .builder()
          .maxAverageBitrate(
            Bitrate.maxAverage
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

  val aacAudioDescription: AudioDescription =
    AudioDescription
      .builder()
      .audioSourceName(SelectorNames.audio)
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
}
