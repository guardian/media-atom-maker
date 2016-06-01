package model

import com.gu.contentatom.thrift._
import atom.media._
import org.scalatest.{ FunSpec, Matchers, Inside }

class ThriftUtilSpec extends FunSpec
    with Matchers
    with Inside {

  def withParamsIt(testName: String, params: (String, String)*)(f: (ThriftUtil) => Unit) =
    it(testName) { f(new ThriftUtil(Map(params: _*))) }

  describe("ThriftUtil") {

    val youtubeId  =  "7H9Z4sn8csA"
    val youtubeUrl = s"https://www.youtube.com/watch?v=${youtubeId}"

    withParamsIt("should result in error if uri param missing") { t =>
      assert(t.parseRequest.isLeft)
    }

    withParamsIt("should correctly identify youtube platform and find id",
                 "uri" -> youtubeUrl) { t =>
      t.parsePlatform should matchPattern { case Right(Platform.Youtube) => }
      t.parseId should matchPattern { case Right(`youtubeId`) => }
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

