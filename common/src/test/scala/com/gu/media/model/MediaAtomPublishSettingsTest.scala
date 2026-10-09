package com.gu.media.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class MediaAtomPublishSettingsTest extends AnyFunSuite with Matchers {
  test("missing YouTube ads setting defaults to false") {
    val settings = Json
      .parse("""{"atomId":"atom","retainYoutubeFurniture":true}""")
      .as[MediaAtomPublishSettings]

    settings.retainYoutubeFurniture must be(true)
    settings.retainYoutubeAds must be(false)
  }
}
