package test

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import controllers.MainApp
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

  val api = new MainApp(dataStore)

  "api" should {
    "return a media atom" in {
      val req = FakeRequest(GET, "/atom/1")
      val result = api.getMediaAtom("1").apply(req)
      status(result) mustEqual OK
    }
  }
}
