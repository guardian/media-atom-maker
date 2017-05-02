package integration

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import integration.services.{Config, GuHttp, TestAtomJsonGenerator}
import play.api.Logger

import scala.collection.mutable.ListBuffer

class IntegrationTestBase extends FunSuite with Matchers with Eventually with IntegrationPatience with GuHttp with TestAtomJsonGenerator with BeforeAndAfterAll {

  val targetBaseUrl: String = Config.targetBaseUrl

  def apiUri(atomId: String): String = s"$targetBaseUrl/api/atom/$atomId"

  var atomStore = new ListBuffer[String]() /* Add all created atoms IDs to this list as first action after atom created. This allows for test cleanup outside the test flow  */
  var youtubeStore = new ListBuffer[String]() /* As above but for YouTube videos upload */

  def deleteAtom(id: String) = {
    Logger.info(s"Deleting atom $id")
    gutoolsDelete(s"$targetBaseUrl/api2/atom/$id")
  }

  def deleteYouTubeVideo(id: String, client: YouTube) = {
    Logger.info(s"Deleting YouTube video $id")

    client.videos()
      .delete(id)
      .setOnBehalfOfContentOwner(Config.youTube.contentOwner)
      .execute()
  }

  override def afterAll(): Unit = {
    atomStore.foreach{ e => deleteAtom(e) }

    if(youtubeStore.nonEmpty) {
      val client = youTubeClient()
      youtubeStore.foreach(deleteYouTubeVideo(_, client))
    }

    super.afterAll()
  }

  def addAtomToStore(atomId: String): ListBuffer[String] = {
    Logger.info(s"Adding $atomId to Atom Store")
    atomStore += atomId
  }

  def addYouTubeVideoToStore(id: String) = {
    Logger.info(s"Adding $id to YouTube Store")
    youtubeStore += id
  }

  private def youTubeClient(): YouTube = {
    val httpTransport = new NetHttpTransport()
    val jacksonFactory = new JacksonFactory()

    val credentials = new GoogleCredential.Builder()
      .setTransport(httpTransport)
      .setJsonFactory(jacksonFactory)
      .setClientSecrets(Config.youTube.clientId, Config.youTube.clientSecret)
      .build
      .setRefreshToken(Config.youTube.refreshToken)

    new YouTube.Builder(httpTransport, jacksonFactory, credentials)
      .setApplicationName(Config.youTube.name)
      .build
  }
}


