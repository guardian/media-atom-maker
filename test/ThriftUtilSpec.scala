package model

import com.gu.contentatom.thrift._
import atom.media._
import org.scalatest.{ FunSpec, Matchers }

class ThriftUtilSpec extends FunSpec with Matchers {

  def withParamsIt(testName: String, params: (String, String)*)(f: (ThriftUtil) => Unit) =
    it(testName) { f(new ThriftUtil(Map(params: _*))) }

  describe("ThriftUtil") {

    val youtubeUrl = "https://www.youtube.com/watch?v=7H9Z4sn8csA"

    withParamsIt("should result in error if uri param missing") { t =>
      assert(t.parseRequest.isLeft)
    }

    withParamsIt("should correctly identify youtube platform",
                 "uri" -> youtubeUrl) { t =>
      t.parsePlatform should matchPattern { case Right(Platform.Youtube) => }
    }
  }
}

