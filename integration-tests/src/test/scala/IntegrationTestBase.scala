package integration

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential

import java.time.Instant
import java.util.UUID
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.gu.media.logging.{Logging, YoutubeApiType, YoutubeRequestLogger, YoutubeRequestType}
import com.gu.media.util.TestFilters
import integration.services.{Config, GuHttp, TestAtomJsonGenerator}
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import play.api.Logger
import play.api.libs.json.Json

class IntegrationTestBase extends FunSuite with Matchers with Eventually with IntegrationPatience with GuHttp with TestAtomJsonGenerator with BeforeAndAfterAll with Logging {

  val targetBaseUrl: String = Config.targetBaseUrl

  def apiUri(atomId: String): String = s"$targetBaseUrl/api/atoms/$atomId"

  // For clean-up after tests
  private var atomIds = List.empty[String]
  private var youTubeIds = List.empty[String]

  def createAtom(): String = {
    val json = generateJson(
      title = s"${TestFilters.testAtomBaseName}-${UUID.randomUUID().toString}",
      description = "test atom",
      category = "News",
      channelId = Config.channelId,
      youtubeCategoryId = Config.youtubeCategoryId,
      expiryDate = Instant.now.toEpochMilli + (100 * 60 * 60 * 24)
    )

    val response = gutoolsPost(s"$targetBaseUrl/api/atoms", jsonBody(json))

    response.code() should be(201)

    val atomId = (Json.parse(response.body().string()) \ "id").get.as[String]

    log.info(s"Adding $atomId to Atom Store")
    atomIds :+= atomId
    atomId
  }

  def addYouTubeVideoToStore(id: String): Unit = {
    youTubeIds :+= id
  }

  override def beforeAll(): Unit = {
    val aliveResponse = gutoolsGet(targetBaseUrl)
    aliveResponse.code() should be (200)
    aliveResponse.body().string() should include ("video")

    super.beforeAll()
  }

  override def afterAll(): Unit = {
    atomIds.foreach { id =>
      Logger.info(s"Deleting atom $id")
      gutoolsDelete(s"$targetBaseUrl/api/atom/$id")
    }

    if(youTubeIds.nonEmpty) {
      val client = youTubeClient()

      youTubeIds.foreach { id =>
        Logger.info(s"Deleting YouTube video $id")

        val request = client.videos()
          .delete(id)
          .setOnBehalfOfContentOwner(Config.youTube.contentOwner)

        YoutubeRequestLogger.logRequest(YoutubeApiType.DataApi, YoutubeRequestType.DeleteVideo)
        request.execute()
      }
    }

    super.afterAll()
  }

  private def youTubeClient(): YouTube = {
    val httpTransport = new NetHttpTransport()
    val jsonFactory = new GsonFactory()

    val credentials = new GoogleCredential.Builder()
      .setTransport(httpTransport)
      .setJsonFactory(jsonFactory)
      .setClientSecrets(Config.youTube.clientId, Config.youTube.clientSecret)
      .build
      .setRefreshToken(Config.youTube.refreshToken)

    new YouTube.Builder(httpTransport, jsonFactory, credentials)
      .setApplicationName(Config.youTube.name)
      .build
  }
}


