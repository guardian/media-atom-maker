package test

import cats.data.Xor
import com.gu.atom.data.{PreviewDataStore, DataStore, VersionConflictError}
import com.gu.atom.play.test.AtomSuite
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import com.gu.contentatom.thrift.ContentAtomEvent
import com.gu.contentatom.thrift.atom.media.Category.{Hosted, News}
import controllers.Api
import data.{PublishedMemoryStore, PreviewMemoryStore, MemoryStore}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.AppendedClues
import org.scalatest.mock.MockitoSugar
import play.api.http.HttpVerbs
import util.atom.MediaAtomImplicits
import play.api.libs.json._
import play.api.test.Helpers._
import test.TestData._

class ApiSpec
    extends AtomSuite
    with AuthTests
    with AppendedClues
    with HttpVerbs
    with MockitoSugar
    with MediaAtomImplicits {

  override def initialPreviewDataStore = new PreviewMemoryStore(Map("1" -> testAtom))
  override def initialLivePublisher = defaultMockPublisher
  override def initialPreviewPublisher = defaultPreviewMockPublisher

  def api(implicit atomConf: AtomTestConf) = atomConf.iget[Api]

  val youtubeId  =  "7H9Z4sn8csA"
  val youtubeUrl = s"https://www.youtube.com/watch?v=${youtubeId}"

  "api" should {

    "return a media atom" in AtomTestConf() { implicit conf =>
      val result = api.getMediaAtom("1").apply(requestWithCookies)
      status(result) mustEqual OK
      val json = contentAsJson(result)
                              (json \ "id").as[String] mustEqual "1"
        (json \ "data" \ "assets").as[List[JsValue]] must have size 2

    }

    "return NotFound for missing atom" in AtomTestConf() { implicit conf =>
      val result = api.getMediaAtom("xyzzy").apply(requestWithCookies)
      status(result) mustEqual NOT_FOUND
    }

    "return not found when adding asset to a non-existant atom" in AtomTestConf() { implicit conf =>
      val req = requestWithCookies.withFormUrlEncodedBody("uri" -> youtubeUrl, "mimetype" -> "", "version" -> "3")
      val result = call(api.addAsset("xyzzy"), req)
      status(result) mustEqual NOT_FOUND
    }

    "complain when catching simultaenous update from datastore" in
    AtomTestConf(previewDataStore = mock[PreviewDataStore]) { implicit conf =>
      val mockDataStore = conf.previewDataStore
      when(mockDataStore.getAtom(any())).thenReturn(Some(testAtom))
      when(mockDataStore.updateAtom(any())).thenReturn(Xor.Left(VersionConflictError(1)))

      val req = requestWithCookies
                .withFormUrlEncodedBody("uri" -> youtubeUrl, "mimetype" -> "", "version" -> "4")
      val result = call(api.addAsset("1"), req)

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockDataStore).updateAtom(any())
    }

    "add an asset to an atom" in AtomTestConf() { implicit conf =>
      val req = requestWithCookies.withFormUrlEncodedBody("uri" -> youtubeUrl, "mimetype" -> "", "version" -> "5")
      val result = call(api.addAsset("1"), req)
      withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual OK }
      val json = contentAsJson(result)
      (json \ "data" \ "assets").as[List[JsValue]] must have size 3
      conf.previewDataStore.getAtom("1").value.tdata.assets must  have size 3
    }

    "create an atom" in AtomTestConf() { implicit conf =>
      val req = requestWithCookies.withFormUrlEncodedBody("id" -> "2")
      val result = call(api.createMediaAtom(), req)
      withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual CREATED  }
      val createdAtom = conf.previewDataStore.getAtom("2").value
      createdAtom.id mustEqual "2"

    }

    "create an atom with default values" in AtomTestConf() { implicit conf =>
      val req = requestWithCookies.withFormUrlEncodedBody("id" -> "3")

      val result = call(api.createMediaAtom(), req)
      withClue(s"(body: [${contentAsString(result)}])") {
        status(result) mustEqual CREATED
      }
      val createdAtom = conf.previewDataStore.getAtom("3").value

      createdAtom.id mustEqual "3"
      val mediaAtom = createdAtom.tdata
      mediaAtom.activeVersion mustEqual Some(1)
      mediaAtom.title mustEqual "unknown"
      mediaAtom.category mustEqual News
      mediaAtom.duration mustEqual None
      mediaAtom.posterUrl mustEqual None
    }

    "create an atom with specified values" in AtomTestConf() { implicit conf =>
      val req = requestWithCookies
                .withFormUrlEncodedBody(
                  "id" -> "4",
                  "title" -> "testing123",
                  "category" -> "hosted",
                  "duration" -> "34",
                  "posterUrl" -> "https://abc/def.jpg"
                )

      val result = call(api.createMediaAtom(), req)
      withClue(s"(body: [${contentAsString(result)}])") {
        status(result) mustEqual CREATED
      }
      val createdAtom = conf.previewDataStore.getAtom("4").value

      createdAtom.id mustEqual "4"
      val mediaAtom = createdAtom.tdata
      mediaAtom.activeVersion mustEqual Some(1)
      mediaAtom.title mustEqual "testing123"
      mediaAtom.category mustEqual Hosted
      mediaAtom.duration mustEqual Some(34)
      mediaAtom.posterUrl mustEqual Some("https://abc/def.jpg")
    }

    "call out to preview publisher when adding an asset" in
    AtomTestConf() { implicit conf =>
      val mockPublisherPreview = conf.previewPublisher
      val eventCaptor = ArgumentCaptor.forClass(classOf[ContentAtomEvent])

      val dataStore = conf.previewDataStore
      val atom = dataStore.getAtom("1").value
      val req = requestWithCookies
                .withFormUrlEncodedBody("uri" -> youtubeUrl, "mimetype" -> "", "version" -> "6")

      val result = call(api.addAsset("1"), req)
      status(result) mustEqual OK


      verify(mockPublisherPreview).publishAtomEvent(eventCaptor.capture())
      val event = eventCaptor.getValue

      event.atom.tdata.assets.length mustEqual 3
    }

    "call report failure if publisher fails" in
    AtomTestConf(livePublisher = failingMockPublisher) { implicit conf =>
      val result = call(api.publishAtom("1"), requestWithCookies)
      status(result) mustEqual INTERNAL_SERVER_ERROR
    }

    "list atoms" in AtomTestConf() { implicit conf =>
      conf.previewDataStore.createAtom(testAtom.copy(id = "2"))
      val result = call(api.listAtoms(), requestWithCookies)
      status(result) mustEqual OK
      contentAsJson(result).as[List[JsValue]] must have size 2
    }

    "change version of atom" in AtomTestConf() { implicit conf =>
      // before...
      conf.previewDataStore.getAtom("1").value.tdata.activeVersion mustEqual Some(2L)
      val result = call(api.revertAtom("1", 1L), requestWithCookies)
      status(result) mustEqual OK
      // after ...
      conf.previewDataStore.getAtom("1").value.tdata.activeVersion mustEqual Some(1L)
    }

    "complain if revert to version without asset" in AtomTestConf() { implicit conf =>
      // before...
      val result = call(api.revertAtom("1", 10L), requestWithCookies)
      status(result) mustEqual INTERNAL_SERVER_ERROR
    }
    
  }
}
