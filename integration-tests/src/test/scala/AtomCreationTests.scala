import java.time.Instant
import java.util.UUID

import integration.IntegrationTestBase
import integration.services.Config
import org.scalatest.CancelAfterFailure
import play.api.libs.json.Json

class AtomCreationTests extends IntegrationTestBase with CancelAfterFailure {

  var atomId: String = ""
  var apiEndpoint: String = ""

  test(s"$targetBaseUrl is up") {
    val response = gutoolsGet(targetBaseUrl)
    response.code() should be (200)
    response.body().string() should include ("video")
  }

  test("Create a new atom") {
    val json = generateJson(
      title = s"test-atom-${UUID.randomUUID().toString}",
      description = "test atom",
      category = "News",
      channelId = Config.channelId,
      youtubeCategoryId = Config.youtubeCategoryId,
      expiryDate = Instant.now.toEpochMilli + (100 * 60 * 60 * 24)
    )

    val response = gutoolsPost(s"$targetBaseUrl/api2/atoms", jsonBody(json))

    response.code() should be(201)

    atomId = (Json.parse(response.body().string()) \ "id").get.as[String]

    addAtomToStore(atomId)

    apiEndpoint = apiUri(atomId)

    eventually {
      gutoolsGet(apiEndpoint).code() should be(200)
    }

    Json.parse(gutoolsGet(s"$targetBaseUrl/api2/atoms/$atomId/published").body().string()).toString() should be("{}")
  }


  test("Add an asset to an existing atom") {
    val assetResponse = gutoolsPost(s"$targetBaseUrl/api2/atoms/$atomId/assets", jsonBody(s"""{"uri":"${Config.asset}"}"""))

    assetResponse.code() should be(200)

    eventually {
      val assets = (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "data" \ "assets") (0)
      (assets \ "id").get.as[String] should be(Config.assetId)
    }
  }

  test("Make an asset current for an existing atom") {
    val currentAssetResponse = gutoolsPut(s"$targetBaseUrl/api2/atom/$atomId/asset-active", Some(jsonBody(s"""{"youtubeId":"${Config.assetId}"}""")))

    currentAssetResponse.code() should be(200)

    eventually {
      (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "defaultHtml").get.as[String] should not be ("<div></div>")
    }
  }

  test("Publishing an existing atom") {
    val publishResponse = gutoolsPut(s"$targetBaseUrl/api2/atom/$atomId/publish")

    publishResponse.code() should be (200)

    eventually {
      (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "contentChangeDetails" \ "published" \ "user" \ "email").get.as[String] should be (Config.userEmail)
    }

  }

}
