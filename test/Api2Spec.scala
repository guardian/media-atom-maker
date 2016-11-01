package test

import com.gu.atom.play.test.AtomSuite
import controllers.Api2
import data.PreviewMemoryStore
import org.scalatest.AppendedClues
import org.scalatest.mock.MockitoSugar
import play.api.http.HttpVerbs
import play.api.libs.json.{JsString, JsObject}
import play.api.test.Helpers._
import util.atom.MediaAtomImplicits
import test.TestData._

class Api2Spec extends AtomSuite
  with AuthTests
  with AppendedClues
  with HttpVerbs
  with MockitoSugar
  with MediaAtomImplicits {

  override def initialPreviewDataStore = new PreviewMemoryStore(Map("1" -> testAtom))
  override def initialLivePublisher = defaultMockPublisher
  override def initialPreviewPublisher = defaultPreviewMockPublisher

  def api2(implicit atomConf: AtomTestConf) = atomConf.iget[Api2]

  val youtubeId  =  "7H9Z4sn8csA"
  val youtubeUrl = s"https://www.youtube.com/watch?v=$youtubeId"

  "api2" should {
    "add an asset to an atom" in AtomTestConf() { implicit conf =>
      val json = JsObject(Seq("uri" -> JsString(youtubeUrl)))

      val result = api2.addAsset("1").apply(requestWithCookies.withJsonBody(json))

      status(result) mustEqual OK
    }
  }
}
