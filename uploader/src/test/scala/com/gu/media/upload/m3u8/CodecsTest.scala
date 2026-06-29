package com.gu.media.upload.m3u8

import com.gu.media.upload.FfMpeg
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class CodecsTest extends AnyFunSuite with Matchers {

  test("Extract avc1 and mp4a from a M3U8 line") {
    Codecs.extractCodecs(
      """#EXT-X-STREAM-INF:BANDWIDTH=568512,AVERAGE-BANDWIDTH=523354,CODECS="avc1.64001e,mp4a.40.2",RESOLUTION=480x854,FRAME-RATE=25.000,SUBTITLES="subs""""
    ) must be(
      Set("avc1.64001e", "mp4a.40.2")
    )
  }

  test("Return empty set if no codecs found") {
    Codecs.extractCodecs(
      """#EXT-X-STREAM-INF:BANDWIDTH=568512,AVERAGE-BANDWIDTH=523354,RESOLUTION=480x854,FRAME-RATE=25.000,SUBTITLES="subs""""
    ) must be(
      Set.empty
    )
  }

  test("Return empty set if line is not an EXT-X-STREAM-INF line") {
    Codecs.extractCodecs(
      """BANDWIDTH=568512,AVERAGE-BANDWIDTH=523354,CODECS="avc1.64001e,mp4a.40.2",RESOLUTION=480x854,FRAME-RATE=25.000,SUBTITLES="subs""""
    ) must be(
      Set.empty
    )
  }

  test("Return all codecs when supplied with multiple lines") {
    Codecs.extractCodecs("""
        |#EXTM3U
        |#EXT-X-VERSION:6
        |#EXT-X-INDEPENDENT-SEGMENTS
        |#EXT-X-STREAM-INF:BANDWIDTH=2172434,AVERAGE-BANDWIDTH=1852542,VIDEO-RANGE=SDR,CODECS="avc1.64001e,mp4a.40.2",RESOLUTION=480x854,FRAME-RATE=25.000,AUDIO="program_audio_0",SUBTITLES="subs"
        |test-video_480w_q8_h264.m3u8
        |#EXT-X-STREAM-INF:BANDWIDTH=541494,AVERAGE-BANDWIDTH=498967,VIDEO-RANGE=SDR,CODECS="avc1.64001e,mp4a.40.2",RESOLUTION=480x854,FRAME-RATE=25.000,AUDIO="program_audio_0",SUBTITLES="subs"
        |test-video_480w_q4_h264.m3u8
        |#EXT-X-STREAM-INF:BANDWIDTH=1700452,AVERAGE-BANDWIDTH=1471917,VIDEO-RANGE=SDR,CODECS="avc1.64001e,mp4a.40.2",RESOLUTION=406x720,FRAME-RATE=25.000,AUDIO="program_audio_0",SUBTITLES="subs"
        |test-video_720h_q8_h264.m3u8
        |#EXT-X-STREAM-INF:BANDWIDTH=466725,AVERAGE-BANDWIDTH=438140,VIDEO-RANGE=SDR,CODECS="avc1.64001e,mp4a.40.2",RESOLUTION=406x720,FRAME-RATE=25.000,AUDIO="program_audio_0",SUBTITLES="subs"
        |test-video_720h_q4_h264.m3u8
        |#EXT-X-STREAM-INF:BANDWIDTH=840891,AVERAGE-BANDWIDTH=782580,VIDEO-RANGE=SDR,CODECS="av01.0.04M.08,mp4a.40.2",RESOLUTION=480x854,FRAME-RATE=25.000,AUDIO="program_audio_0",SUBTITLES="subs"
        |test-video_480w_q8_av1.m3u8
        |#EXT-X-STREAM-INF:BANDWIDTH=304684,AVERAGE-BANDWIDTH=294781,VIDEO-RANGE=SDR,CODECS="av01.0.04M.08,mp4a.40.2",RESOLUTION=480x854,FRAME-RATE=25.000,AUDIO="program_audio_0",SUBTITLES="subs"
        |test-video_480w_q4_av1.m3u8
        |#EXT-X-STREAM-INF:BANDWIDTH=695278,AVERAGE-BANDWIDTH=655085,VIDEO-RANGE=SDR,CODECS="av01.0.04M.08,mp4a.40.2",RESOLUTION=406x720,FRAME-RATE=25.000,AUDIO="program_audio_0",SUBTITLES="subs"
        |test-video_720h_q8_av1.m3u8
        |#EXT-X-STREAM-INF:BANDWIDTH=283632,AVERAGE-BANDWIDTH=275045,VIDEO-RANGE=SDR,CODECS="av01.0.04M.08,mp4a.40.2",RESOLUTION=406x720,FRAME-RATE=25.000,AUDIO="program_audio_0",SUBTITLES="subs"
        |test-video_720h_q4_av1.m3u8
        |#EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID="subs",NAME="English",DEFAULT=YES,AUTOSELECT=YES,FORCED=NO,LANGUAGE="eng",URI="test-video_captions.m3u8"
        |#EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="program_audio_0",LANGUAGE="eng",NAME="Alternate Audio",AUTOSELECT=YES,DEFAULT=YES,CHANNELS="2",URI="test-video_audio.m3u8"
        |""".stripMargin.split("\n").iterator) must be(
      Set("avc1.64001e", "mp4a.40.2", "av01.0.04M.08")
    )
  }
}
