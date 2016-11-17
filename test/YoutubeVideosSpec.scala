package test

import com.gu.atom.play.test.AtomSuite
import controllers.YoutubeVideos
import model.VideoLicense
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
      val metadataToUpdate = JsObject(Seq(
        "description" -> JsString(s"This is a new description: ${DateTime.now()}"),
        "categoryId" -> JsString("1"),
        "license" -> JsString(VideoLicense.Youtube),
        "tags" -> JsArray(Seq(JsString("test"), JsString("video")))))
      val result = youtubeVideos.update(youtubeId).apply(requestWithCookies.withJsonBody(metadataToUpdate))
      status(result) mustEqual OK
    }
  }
}
