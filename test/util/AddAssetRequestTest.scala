package util

import org.scalatest.{FunSuite, MustMatchers}
import play.api.libs.json.Json

class AddAssetRequestTest extends FunSuite with MustMatchers {
  test("YouTube asset") {
    val input =
      s"""{
         |    "platform": "Youtube",
         |    "assets": [
         |        { "id": "QRplDNMsS4U" }
         |    ]
         |}
       """.stripMargin

    val asset :: Nil = AddAssetRequest(Json.parse(input)).get
    asset.platform must be(model.Platform.Youtube)
    asset.id must be("QRplDNMsS4U")
  }

  test("Error on multiple YouTube assets") {
    val input = s"""{
                   |    "platform": "Youtube",
                   |    "assets": [
                   |        { "id": "QRplDNMsS4U" },
                   |        { "id": "2cyNQhHbtT4" }
                   |    ]
                   |}
       """.stripMargin

    AddAssetRequest(Json.parse(input)).isError must be(true)
  }

  test("Url assets") {
    val input =
      s"""{
         |    "platform": "Url",
         |    "assets": [
         |        { "uri": "link_1", "mimeType": "video/mp4" },
         |        { "uri": "link_2", "mimeType": "video/vp8" }
         |    ]
         |}
       """.stripMargin

    val asset1 :: asset2 :: Nil = AddAssetRequest(Json.parse(input)).get

    asset1.platform must be(model.Platform.Url)
    asset1.id must be("link_1")
    asset1.mimeType must contain("video/mp4")

    asset2.platform must be(model.Platform.Url)
    asset2.id must be("link_2")
    asset2.mimeType must contain("video/vp8")
  }

  test("Fail on bad Url asset") {
    val input =
      s"""{
         |    "platform": "Url",
         |    "assets": [
         |        { "uri": "link_1", "mimeType": "video/mp4" },
         |        { "uri": "link_2" }
         |    ]
         |}
       """.stripMargin

    AddAssetRequest(Json.parse(input)).isError must be(true)
  }

  test("Unknown platform") {
    val input =
      s"""{
         |    "platform": "Fake",
         |    "assets": [
         |        { "uri": "link_1", "mimeType": "video/mp4" },
         |        { "uri": "link_2" }
         |    ]
         |}
       """.stripMargin

    AddAssetRequest(Json.parse(input)).isError must be(true)
  }

  test("Only URI") {
    val input =
      s"""{
         |    "uri": "https://www.youtube.com/watch?v=QRplDNMsS4U"
         |}
       """.stripMargin

    val asset :: Nil = AddAssetRequest(Json.parse(input)).get
    asset.platform must be(model.Platform.Youtube)
    asset.id must be("QRplDNMsS4U")
  }

  test("Fail if YouTube URI passed as ID") {
    val input =
      s"""{
         |    "platform": "Youtube",
         |    "assets": [
         |        { "id": "https://www.youtube.com/watch?v=QRplDNMsS4U" }
         |    ]
         |}
       """.stripMargin

    AddAssetRequest(Json.parse(input)).isError must be(true)
  }
}
