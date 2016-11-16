package test

import com.gu.atom.play.test.AtomSuite
import controllers.YoutubeVideos
import org.joda.time.DateTime
import org.scalatest.AppendedClues
import org.scalatest.mock.MockitoSugar
import play.api.http.HttpVerbs
import play.api.libs.json.{JsArray, JsString, JsObject}
import play.api.test.Helpers._
import util.atom.MediaAtomImplicits

class YoutubeVideosSpec extends AtomSuite
  with AuthTests
  with AppendedClues
  with HttpVerbs
  with MockitoSugar
  with MediaAtomImplicits {

  def youtubeVideos(implicit atomConf: AtomTestConf) = atomConf.iget[YoutubeVideos]

  val youtubeId  =  "eNqP0sUTTLQ"

  "YoutubeVideos" should {
    "update assets in YouTube" in AtomTestConf() { implicit conf =>
      val descriptionText = s"This is a new description: ${DateTime.now()}"
      val metadataToUpdate = JsObject(Seq("description" -> JsString(descriptionText), "tags" -> JsArray(Seq(JsString("newTag")))))
      val result = youtubeVideos.update(youtubeId).apply(requestWithCookies.withJsonBody(metadataToUpdate))
      status(result) mustEqual OK
    }
  }
}
