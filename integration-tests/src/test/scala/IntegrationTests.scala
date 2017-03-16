package integration

import com.squareup.okhttp._
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import integration.services.{Config, GuHttp, TestAtomJsonGenerator}
import java.time.Instant
import java.util.UUID

import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer

class IntegrationTests extends FlatSpec with Matchers with Eventually with IntegrationPatience with GuHttp with TestAtomJsonGenerator with BeforeAndAfterAll {

  val targetBaseUrl: String = Config.targetBaseUrl
  val JSON = MediaType.parse("application/json; charset=utf-8")

  def apiUri(atomId: String): String = s"$targetBaseUrl/api/atom/$atomId"

  var createdAtoms = new ListBuffer[String]() /* Add all created atoms IDs to this list as first action after atom created. This allows for test cleanup outside the test flow  */

  def deleteAtom(id: String) = {
    gutoolsDelete(s"$targetBaseUrl/api2/atom/$id")
  }

  override def afterAll(): Unit = {
    createdAtoms.foreach{ e => deleteAtom(e) }
    super.afterAll()
  }

  "Hitting code atom maker" should "return a 200" in {
    val response = gutoolsGet(targetBaseUrl)
    response.code() should be (200)
    response.body().string() should include ("video")
  }

  "Creating a new atom, adding an asset and making it the current asset" should "be represented in the atom maker API" in {
    val asset = Config.asset
    val assetId = Config.assetId
    val json = generateJson(
      title = s"test-atom-${UUID.randomUUID().toString}",
      description = "test atom",
      category = "News",
      channelId = Config.channelId,
      youtubeCategoryId = Config.youtubeCategoryId,
      expiryDate = Instant.now.toEpochMilli + (100 * 60 * 60 * 24)
    )

    /* Create the atom */

    val body = RequestBody.create(JSON, json)
    val response = gutoolsPost(s"$targetBaseUrl/api2/atoms", Some(body))

    response.code() should be (201)

    val atomId = (Json.parse(response.body().string()) \ "id").get.as[String]

    createdAtoms += atomId

    println(s"Create atom test: Atom Id = $atomId")

    val apiEndpoint = apiUri(atomId)

    eventually {
      gutoolsGet(apiEndpoint).code() should be (200)
    }

    Json.parse(gutoolsGet(s"$targetBaseUrl/api2/atoms/$atomId/published").body().string()).toString() should be ("{}")

    /* Add the asset */

    val assetBody = RequestBody.create(JSON, s"""{"uri":"$asset"}""")

    val assetResponse = gutoolsPost(s"${targetBaseUrl}/api2/atoms/${atomId}/assets", Some(assetBody))

    assetResponse.code() should be (200)

    eventually {
      val assets = (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "data" \ "assets")(0)
      (assets \ "id").get.as[String] should be (assetId)
    }

    /* Make current asset */

    val currentAssetBody = RequestBody.create(JSON, s"""{"youtubeId":"$assetId"}""")

    val currentAssetResponse = gutoolsPut(s"${targetBaseUrl}/api2/atom/${atomId}/asset-active", Some(currentAssetBody))

    currentAssetResponse.code() should be (200)

    eventually {
      (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "defaultHtml").get.as[String] should not be ("<div></div>")
    }

    /* Publish atom */

    val publishResponse = gutoolsPut(s"${targetBaseUrl}/api2/atom/${atomId}/publish")

    publishResponse.code() should be (200)

    eventually {
      (Json.parse(gutoolsGet(apiEndpoint).body().string()) \ "contentChangeDetails" \ "published" \ "user" \ "email").get.as[String] should be (Config.userEmail)
    }

  }

}


