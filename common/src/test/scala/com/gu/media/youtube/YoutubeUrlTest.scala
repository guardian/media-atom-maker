package com.gu.media.youtube

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class YoutubeUrlTest extends AnyFunSuite with Matchers {
  test("Extract video id from standard YouTube URL") {
    YoutubeUrl.parse("https://www.youtube.com/watch?v=CqUDO-livlc") must be(
      Some("CqUDO-livlc")
    )
  }

  test("Extract video id from standard YouTube URL with extra param") {
    YoutubeUrl.parse(
      "https://www.youtube.com/watch?v=CqUDO-livlc&feature=youtu.be"
    ) must be(Some("CqUDO-livlc"))
  }

  test("Extract video id from standard YouTube URL with multiple extra param") {
    YoutubeUrl.parse(
      "https://www.youtube.com/watch?v=CqUDO-livlc&feature=youtu.be&rel=0"
    ) must be(Some("CqUDO-livlc"))
  }

  test("Extract video id from YouTube short URL") {
    YoutubeUrl.parse("https://youtu.be/CqUDO-livlc") must be(
      Some("CqUDO-livlc")
    )
  }

  test("Extract video id from YouTube embed URL") {
    YoutubeUrl.parse("https://www.youtube.com/embed/CqUDO-livlc") must be(
      Some("CqUDO-livlc")
    )
  }

  test("Extract video id from YouTube embed URL with extra params") {
    YoutubeUrl.parse("https://www.youtube.com/embed/CqUDO-livlc?rel=0") must be(
      Some("CqUDO-livlc")
    )
  }

  test("Extract video id from YouTube embed URL with multiple extra params") {
    YoutubeUrl.parse(
      "https://www.youtube.com/embed/CqUDO-livlc?rel=0&autoplay=1"
    ) must be(Some("CqUDO-livlc"))
  }

  test("Extract video id from YouTube live URL with extra param") {
    YoutubeUrl.parse(
      "https://www.youtube.com/live/CqUDO-livlc?feature=share"
    ) must be(Some("CqUDO-livlc"))
  }

  test("Extract video id from YouTube live URL missing www.") {
    YoutubeUrl.parse(
      "https://youtube.com/live/CqUDO-livlc?feature=share"
    ) must be(Some("CqUDO-livlc"))
  }

  test("Fail to extract video id from invalid URL") {
    YoutubeUrl.parse("https://www.foo.com/watch?v=CqUDO-livlc") must be(None)
  }

  test("Fail to extract video id from YouTube URL as video id is too short") {
    YoutubeUrl.parse("https://www.youtube.com/watch?v=CqUDO-livl") must be(None)
  }
}
