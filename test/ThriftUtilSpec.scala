package test

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import com.gu.media.util.ThriftUtil
import org.jsoup.Jsoup
import org.scalatest.Inside
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.xml.XML

class ThriftUtilSpec extends AnyFunSpec with Matchers with Inside {

  describe("ThriftUtil") {

    import ThriftUtil._

    def makeParams(params: (String, String)*): Map[String, Seq[String]] =
      params.map { case (k, v) => (k, List(v)) }.toMap

    val youtubeId = "7H9Z4sn8csA"
    val youtubeUrl = s"https://www.youtube-nocookie.com/watch?v=${youtubeId}"

    it("should result in error if uri param is invalid") {
      assert(parseRequest(makeParams("uri" -> "gobbldeygook")).isLeft)
    }

    it("should correctly identify youtube platform and find id") {
      parsePlatform(youtubeUrl) should matchPattern {
        case Right(Platform.Youtube) =>
      }
      parseId(youtubeUrl) should matchPattern { case Right(`youtubeId`) => }
    }

    it("should correctly generate media atom data") {
      inside(parseMediaAtom(makeParams("uri" -> youtubeUrl))) {
        case Right(
              atom: MediaAtom
            ) =>
          atom.assets should have length 1
          atom.assets.head should equal(
            Asset(AssetType.Video, 1L, youtubeId, Platform.Youtube)
          )
      }
    }

    it("should create empty assets without uri") {
      inside(parseMediaAtom(Map.empty)) {
        case Right(
              atom: MediaAtom
            ) =>
          atom.assets shouldBe Nil
      }
    }

    it("should create multiple assets with multiple uri params") {
      inside(parseMediaAtom(Map("uri" -> List(youtubeUrl, youtubeUrl)))) {
        case Right(
              atom: MediaAtom
            ) =>
          atom.assets should have length 2
      }
    }

    it("should correctly generate atom") {
      inside(parseRequest(makeParams("uri" -> youtubeUrl))) {
        case Right(atom: Atom) => {
          atom.contentChangeDetails should matchPattern {
            case ContentChangeDetails(
                  None,
                  None,
                  None,
                  1L,
                  None,
                  None,
                  None,
                  None
                ) =>
          }

          val iframe = Jsoup.parse(atom.defaultHtml).getElementsByTag("iframe")
          iframe.attr("src") should be(
            s"https://www.youtube-nocookie.com/embed/$youtubeId?showinfo=0&rel=0"
          )
        }
      }
    }

    it("should correctly generate media atom with metadata") {
      val meta =
        "{\"channelId\":\"channelId\",\"commentsEnabled\":true,\"privacyStatus\":\"private\",\"expiryDate\":1}"

      inside(
        parseMediaAtom(makeParams("uri" -> youtubeUrl, "metadata" -> meta))
      ) {
        case Right(
              atom: MediaAtom
            ) =>
          atom.metadata.get.commentsEnabled should be(Some(true))
          atom.metadata.get.channelId should be(Some("channelId"))
          atom.metadata.get.privacyStatus should be(Some(PrivacyStatus.Private))
          atom.metadata.get.expiryDate should be(Some(1))
      }
    }
  }
}
