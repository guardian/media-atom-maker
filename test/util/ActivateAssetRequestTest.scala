package util

import org.scalatest.{FunSuite, MustMatchers}
import play.api.libs.json.Json

class ActivateAssetRequestTest extends FunSuite with MustMatchers {
  test("activate by version") {
    val input = """ { "version": 5 }"""
    val request = ActivateAssetRequest(Json.parse(input)).get

    request mustBe an [ActivateAssetByVersion]
    request.asInstanceOf[ActivateAssetByVersion].version must be(5)
  }

  test("activate by id") {
    val input = """ { "youtubeId": "https://www.youtube.com/watch?v=QRplDNMsS4U" }"""
    val request = ActivateAssetRequest(Json.parse(input)).get

    request mustBe an [ActivateYouTubeAssetById]
    request.asInstanceOf[ActivateYouTubeAssetById].id must be("https://www.youtube.com/watch?v=QRplDNMsS4U")
  }
}
