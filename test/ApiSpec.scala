package test

import data.AtomPublisher
import org.scalatest.mock.MockitoSugar
import play.api.libs.json._
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import controllers.Api
import org.scalatestplus.play._
import play.api.test._
import play.api.http.HttpVerbs
import play.api.test.Helpers._
import data.MemoryStore
import model.ThriftUtil._
import org.scalatest.AppendedClues

class ApiSpec extends PlaySpec
    with OneAppPerSuite
    with AppendedClues
    with HttpVerbs
    with MockitoSugar {

  implicit lazy val materializer = app.materializer

  val dataStore = new MemoryStore(
    Map(
      "1" -> Atom(
        id = "1",
        atomType = AtomType.Media,
        defaultHtml = "<div></div>",
        data = AtomData.Media(
          MediaAtom(
            activeVersion = 1L
          )
        ),
        contentChangeDetails = ContentChangeDetails(revision = 1)
      )
    )
  )

  val youtubeId  =  "7H9Z4sn8csA"
  val youtubeUrl = s"https://www.youtube.com/watch?v=${youtubeId}"

  val mockPublisher = mock[AtomPublisher]

  val api = new Api(dataStore, mockPublisher)

  "api" should {
    "return a media atom" in {
      val result = api.getMediaAtom("1").apply(FakeRequest())
      status(result) mustEqual OK
      val json = contentAsJson(result)
                              (json \ "id").as[String] mustEqual "1"
        (json \ "data" \ "assets").as[List[JsValue]] must have size 0

    }
    "return NotFound for missing atom" in {
      val result = api.getMediaAtom("xyzzy").apply(FakeRequest())
      status(result) mustEqual NOT_FOUND
    }
    "return not found when adding asset to a non-existant atom" in {
      val req = FakeRequest().withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "1")
      val result = call(api.addAsset("xyzzy"), req)
      status(result) mustEqual NOT_FOUND
    }
    "add an asset to an atom" in {
      val req = FakeRequest().withFormUrlEncodedBody("uri" -> youtubeUrl, "version" -> "1")
      val result = call(api.addAsset("1"), req)
      withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual CREATED }
      dataStore.getMediaAtom("1").value.mediaData.assets must have size 1
    }
    "create an atom" in {
      val req = FakeRequest().withFormUrlEncodedBody("id" -> "2")
      val result = call(api.createMediaAtom(), req)
      withClue(s"(body: [${contentAsString(result)}])") { status(result) mustEqual CREATED  }
      val createdAtom = dataStore.getMediaAtom("2").value
      createdAtom.id mustEqual "2"
    }
  }
}
