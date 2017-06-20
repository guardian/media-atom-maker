package test

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import org.scalatest.{FunSpec, Inside, Matchers}
import util.ThriftUtil

import scala.xml.XML

class ThriftUtilSpec extends FunSpec
    with Matchers
    with Inside {

  describe("ThriftUtil") {

    import ThriftUtil._

    def makeParams(params: (String, String)*): Map[String, Seq[String]] =
      params.map { case (k, v) => (k, List(v)) }.toMap

    val youtubeId  =  "7H9Z4sn8csA"
    val youtubeUrl = s"https://www.youtube.com/watch?v=${youtubeId}"

    it("should result in error if uri param is invalid") {
      assert(parseRequest(makeParams("uri" -> "gobbldeygook")).isLeft)
    }

    it("should correctly identify youtube platform and find id") {
      parsePlatform(youtubeUrl) should matchPattern { case Right(Platform.Youtube) => }
      parseId(youtubeUrl) should matchPattern { case Right(`youtubeId`) => }
    }

    it("should correctly generate media atom data") {
      inside(parseMediaAtom(makeParams("uri" -> youtubeUrl))) {
        case Right(MediaAtom(assets, Some(1L), "unknown", Category.News, None, None, None, None, None, None, None, None,
        None, None, None)) =>
          assets should have length 1
          assets.head should equal(Asset(AssetType.Video, 1L, youtubeId, Platform.Youtube))
      }
    }

    it("should create empty assets without uri") {
      parseMediaAtom(Map.empty) should matchPattern { case Right(MediaAtom(Nil, _, _, _, _, _, _, _, _, _, _, _, _, _, _)) => }
    }

    it("should create multiple assets with multiple uri params") {
      inside(parseMediaAtom(Map("uri" -> List(youtubeUrl, youtubeUrl)))) {
        case Right(MediaAtom(assets, _, _, _, _, _, _, _, _, _, _, _, _, _, _)) =>
          assets should have length 2
      }
    }

    it("should correctly generate atom") {
      inside(parseRequest(makeParams("uri" -> youtubeUrl))) {
        case Right(atom) =>
          inside(atom) {
            case Atom(_, AtomType.Media, Nil, defaultHtml, _, changeDetails, None, _) =>
              changeDetails should matchPattern {
                case ContentChangeDetails(None, None, None, 1L, None) =>
              }
              XML.loadString(defaultHtml) should matchPattern {
                case <iframe>{_}</iframe> =>
              }
          }
      }
    }

    it("should correctly generate media atom with metadata") {
      val meta = "{\"channelId\":\"channelId\",\"commentsEnabled\":true,\"privacyStatus\":\"private\",\"expiryDate\":1}"

      inside(parseMediaAtom(makeParams("uri" -> youtubeUrl, "metadata" -> meta))) {
        case Right(MediaAtom(assets, Some(1L), "unknown", Category.News, None, None, None, None, None, metadata, None,
        None, None, None, None)) =>
          metadata should matchPattern { case Some(Metadata(_, _, _, Some(true), Some("channelId"), Some(PrivacyStatus.Private), Some(1), _)) => }
      }
    }
  }
}

