package test

import play.api.libs.json._
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import controllers.Api
import org.scalatestplus.play._
import play.api.test._
import play.api.http.HttpVerbs
import play.api.test.Helpers._
import data.MemoryStore

class ApiSpec extends PlaySpec
    with OneAppPerTest
    with HttpVerbs {

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

  val api = new Api(dataStore)

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
  }
}
