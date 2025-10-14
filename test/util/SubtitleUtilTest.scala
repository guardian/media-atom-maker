package util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SubtitleUtilTest extends AnyFlatSpec with Matchers {

  "contentTypeForFilename" should "return the content type based on file extension" in {
    SubtitleUtil.contentTypeForFilename(
      "filename.srt"
    ) shouldBe "application/x-subrip"
    SubtitleUtil.contentTypeForFilename(
      "/some/path/filename.srt"
    ) shouldBe "application/x-subrip"
    SubtitleUtil.contentTypeForFilename(
      "filename.vtt.SRT"
    ) shouldBe "application/x-subrip"
    SubtitleUtil.contentTypeForFilename("foo.vtt") shouldBe "text/vtt"
    SubtitleUtil.contentTypeForFilename(
      "/some/path/foo.VTT"
    ) shouldBe "text/vtt"
    SubtitleUtil.contentTypeForFilename(
      "/some/path/foo.bar"
    ) shouldBe "application/octet-stream"
  }
}
