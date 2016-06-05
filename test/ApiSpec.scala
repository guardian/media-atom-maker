package test

import data.{ AtomPublisher, DataStore }
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.libs.json._
import controllers.Api
import org.scalatestplus.play._
import play.api.test._
import play.api.http.HttpVerbs
import play.api.test.Helpers._
import data.MemoryStore
import model.ThriftUtil._
import org.scalatest.AppendedClues
import scala.util.{ Success, Failure }

import TestData._

class ApiSpec extends PlaySpec
    with OneAppPerSuite
    with AppendedClues
    with HttpVerbs
    with MockitoSugar {

  implicit lazy val materializer = app.materializer

  val dataStore = new MemoryStore(Map("1" -> testAtom))

  val youtubeId  =  "7H9Z4sn8csA"
  val youtubeUrl = s"https://www.youtube.com/watch?v=${youtubeId}"

  def defaultMockPublisher: AtomPublisher = {
    val p = mock[AtomPublisher]
    when(p.publishAtomEvent(any())).thenReturn(Success(()))
    p
  }

  def failingMockPublisher: AtomPublisher = {
    val p = mock[AtomPublisher]
    when(p.publishAtomEvent(any())).thenReturn(Failure(new Exception("failure")))
    p
  }

  def withApi(dataStore: DataStore = dataStore,
              publisher: AtomPublisher = defaultMockPublisher)(block: Api => Unit) =
    block(new Api(dataStore, publisher))

  "api" should {
    "return a media atom" in withApi() { api =>
      val result = api.getMediaAtom("1").apply(FakeRequest())
      status(result) mustEqual OK
      val json = contentAsJson(result)
                              (json \ "id").as[String] mustEqual "1"
        (json \ "data" \ "assets").as[List[JsValue]] must have size 0

    }
    "return NotFound for missing atom" in withApi() { api =>
      val result = api.getMediaAtom("xyzzy").apply(FakeRequest())
      status(result) mustEqual NOT_FOUND
    }
    "return not found when adding asset to a non-existant atom" in withApi() { api =>
      val req = FakeRequest().withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "1")
      val result = call(api.addAsset("xyzzy"), req)
      status(result) mustEqual NOT_FOUND
    }
    "add an asset to an atom" in withApi() { api =>
      val req = FakeRequest().withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "1")
      val result = call(api.addAsset("1"), req)
      withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual CREATED }
      dataStore.getMediaAtom("1").value.mediaData.assets must have size 1
    }
    "create an atom" in withApi() { api =>
      val req = FakeRequest().withFormUrlEncodedBody("id" -> "2")
      val result = call(api.createMediaAtom(), req)
      withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual CREATED  }
      val createdAtom = dataStore.getMediaAtom("2").value
      createdAtom.id mustEqual "2"
    }
    "call out to publisher to publish an atom" in withApi() { api =>
      val result = call(api.publishAtom("1"), FakeRequest())
      status(result) mustEqual NO_CONTENT
    }
    "call report failure if publisher fails" in withApi(publisher = failingMockPublisher) { api =>
      val result = call(api.publishAtom("1"), FakeRequest())
      status(result) mustEqual INTERNAL_SERVER_ERROR
    }
  }
}
