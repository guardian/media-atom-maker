package model

import com.gu.contentatom.thrift._
import atom.media._
import org.scalatest.{ FunSpec, Matchers, Inside }
import scala.xml.{ Text, XML }

class ThriftUtilSpec extends FunSpec
    with Matchers
    with Inside {

  describe("ThriftUtil") {

    import ThriftUtil._

    def makeParams(params: (String, String)*): Map[String, Seq[String]] =
      (params.map { case (k,v) => (k, List(v)) }).toMap

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
        case Right(MediaAtom(assets, 1L, None)) =>
          assets should have length 1
          assets.head should equal(Asset(AssetType.Video, 1L, youtubeId, Platform.Youtube))
      }
    }

    it("should create empty assets without uri") {
      parseMediaAtom(Map.empty) should matchPattern { case Right(MediaAtom(Nil, _, _)) => }
    }

    it("should create multiple assets with multiple uri params") {
      inside(parseMediaAtom(Map("uri" -> List(youtubeUrl, youtubeUrl)))) {
        case Right(MediaAtom(assets, _, _)) =>
          assets should have length 2
      }
    }

    it("should correctly generate atom") {
      inside(parseRequest(makeParams("uri" -> youtubeUrl))) {
        case Right(atom) =>
          inside(atom) {
            case Atom(_, AtomType.Media, Nil, defaultHtml, _, changeDetails, None) =>
              changeDetails should matchPattern {
                case ContentChangeDetails(None, None, None, 1L) =>
              }
              XML.loadString(defaultHtml) should matchPattern {
                case <iframe>{_}</iframe> =>
              }
          }
      }
    }
  }
}

