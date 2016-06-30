package test

import cats.data.Xor
import data.{ AtomPublisher, DataStore, VersionConflictError }
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.mvc.{ AnyContent, Cookie, Request }

import util.atom.MediaAtomImplicits

import play.api.libs.json._
import controllers.Api
import org.scalatestplus.play._
import play.api.test._
import play.api.http.HttpVerbs
import play.api.test.Helpers._
import data.MemoryStore

import org.scalatest.AppendedClues
import scala.util.{ Success, Failure }

import com.gu.pandomainauth.model._

import java.util.Date

import TestData._

class ApiSpec
    extends MediaAtomSuite
    with AppendedClues
    with HttpVerbs
    with MockitoSugar
    with MediaAtomImplicits {

  implicit lazy val materializer = app.materializer

  def initialDataStore = new MemoryStore(Map("1" -> testAtom))

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

  def testUser: AuthenticatedUser = AuthenticatedUser(
    user = User("Homer", "Simpson", "homer.simpson@guardian.co.uk", None),
    authenticatingSystem = "test",
    authenticatedIn = Set("test"),
    expires = new Date().getTime + oneHour,
    multiFactor = true
  )

  def requestWithCookies(api: Api, user: AuthenticatedUser = testUser) =
    FakeRequest().withCookies(api.authActions.generateCookies(testUser): _*)

  def withApi(dataStore: DataStore = initialDataStore,
              publisher: AtomPublisher = defaultMockPublisher)
             (block: Api => Unit) =
    block(getApi(dataStore, publisher))

  "api" should {
    "return a media atom" in withApi() { api =>
      val result = api.getMediaAtom("1").apply(requestWithCookies(api))
      status(result) mustEqual OK
      val json = contentAsJson(result)
                              (json \ "id").as[String] mustEqual "1"
        (json \ "data" \ "assets").as[List[JsValue]] must have size 2

    }
    "return NotFound for missing atom" in withApi() { api =>
      val result = api.getMediaAtom("xyzzy").apply(requestWithCookies(api))
      status(result) mustEqual NOT_FOUND
    }
    "return not found when adding asset to a non-existant atom" in withApi() { api =>
      val req = requestWithCookies(api).withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "3")
      val result = call(api.addAsset("xyzzy"), req)
      status(result) mustEqual NOT_FOUND
    }

    "complain when catching simultaenous update from datastore" in {
      val mockDataStore = mock[DataStore]
      when(mockDataStore.getMediaAtom(any())).thenReturn(Some(testAtom))
      when(mockDataStore.updateMediaAtom(any())).thenReturn(Xor.Left(VersionConflictError(1)))
      withApi(dataStore = mockDataStore) { api =>
        val req = requestWithCookies(api)
          .withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "1")
        val result = call(api.addAsset("1"), req)

        status(result) mustEqual INTERNAL_SERVER_ERROR
        verify(mockDataStore).updateMediaAtom(any())
      }
    }

    "add an asset to an atom" in {
      val dataStore = initialDataStore
      withApi(dataStore = dataStore) { api =>
        val req = requestWithCookies(api).withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "1")
        val result = call(api.addAsset("1"), req)
        withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual CREATED }
        dataStore.getMediaAtom("1").value.tdata.assets must have size 3
      }
    }
    "create an atom" in {
      val dataStore = initialDataStore
      withApi(dataStore = dataStore) { api =>
        val req = requestWithCookies(api).withFormUrlEncodedBody("id" -> "2")
        val result = call(api.createMediaAtom(), req)
        withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual CREATED  }
        val createdAtom = dataStore.getMediaAtom("2").value
        createdAtom.id mustEqual "2"
      }
    }
    "call out to publisher to publish an atom" in withApi() { api =>
      val result = call(api.publishAtom("1"), requestWithCookies(api))
      status(result) mustEqual NO_CONTENT
    }
    "call report failure if publisher fails" in withApi(publisher = failingMockPublisher) { api =>
      val result = call(api.publishAtom("1"), requestWithCookies(api))
      status(result) mustEqual INTERNAL_SERVER_ERROR
    }
    "should list atoms" in {
      val dataStore = initialDataStore
      dataStore.createMediaAtom(testAtom.copy(id = "2"))
      withApi(dataStore = dataStore) { api =>
        val result = call(api.listAtoms(), requestWithCookies(api))
        status(result) mustEqual OK
        contentAsJson(result).as[List[JsValue]] must have size 2
      }
    }
    "should change version of atom" in {
      val dataStore = initialDataStore
      withApi(dataStore = dataStore) { api =>
        // before...
        dataStore.getMediaAtom("1").value.tdata.activeVersion mustEqual 2L
        val result = call(api.revertAtom("1", 1L), requestWithCookies(api))
        status(result) mustEqual OK
        // after ...
        dataStore.getMediaAtom("1").value.tdata.activeVersion mustEqual 1L
      }
    }
    "should complain if revert to version without asset" in {
      val dataStore = initialDataStore
      withApi(dataStore = dataStore) { api =>
        // before...
        val result = call(api.revertAtom("1", 10L), requestWithCookies(api))
        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }
}
