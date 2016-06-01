package model

import com.gu.contentatom.thrift._
import atom.media._
import org.scalatest.{ FunSpec, Matchers, Inside }

class ThriftUtilSpec extends FunSpec
    with Matchers
    with Inside {

  def withParamsIt(testName: String, singleParams: (String, String)*)
                  (f: (ThriftUtil) => Unit) = {
    val params = singleParams.map {
      case (name, value) => name -> List(value)
    }
    it(testName) { f(new ThriftUtil(Map(params: _*))) }
  }

  describe("ThriftUtil") {

    val youtubeId  =  "7H9Z4sn8csA"
    val youtubeUrl = s"https://www.youtube.com/watch?v=${youtubeId}"

    withParamsIt("should result in error if uri param is invalid",
                 "uri" -> "gobbldeygook") { t =>
      assert(t.parseRequest.isLeft)
    }

    withParamsIt("should correctly identify youtube platform and find id") { t =>
      t.parsePlatform(youtubeUrl) should matchPattern { case Right(Platform.Youtube) => }
      t.parseId(youtubeUrl) should matchPattern { case Right(`youtubeId`) => }
    }

    withParamsIt("should use default version of 1") { t =>
      t.parseVersion should equal(1L)
    }

    withParamsIt("should allow overriding of version",
                 "version" -> "123") { t =>
      t.parseVersion should equal(123L)
    }

    withParamsIt("should correctly generate media atom data",
                 "uri" -> youtubeUrl) { t =>
      inside(t.parseMediaAtom) {
        case Right(MediaAtom(assets, 1L, None)) =>
          assets should have length 1
          assets.head should equal(Asset(AssetType.Video, 1L, youtubeId, Platform.Youtube))
      }
    }

    withParamsIt("should create empty assets without uri") { t =>
      t.parseMediaAtom should matchPattern { case Right(MediaAtom(Nil, _, _)) => }
    }

    it("should create multiple assets with multiple uri params") {
      val t = new ThriftUtil(Map("uri" -> List(youtubeUrl, youtubeUrl)))
      inside(t.parseMediaAtom) {
        case Right(MediaAtom(assets, _, _)) =>
          assets should have length 2
      }
    }

    withParamsIt("should correctly generate atom",
                 "uri" -> youtubeUrl) { t =>
      inside(t.parseRequest) {
        case Right(atom) =>
          inside(atom) {
            case Atom(_, AtomType.Media, Nil, "<div></div>", _, changeDetails, None) =>
              changeDetails should matchPattern {
                case ContentChangeDetails(None, None, None, 1L) =>
              }
          }
      }
    }
  }
}

