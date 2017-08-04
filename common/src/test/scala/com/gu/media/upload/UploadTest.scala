package com.gu.media.upload

import org.scalatest.{FunSuite, MustMatchers}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import com.gu.media.upload.model.Upload._
import org.scalacheck.Gen

class UploadTest extends FunSuite with MustMatchers with GeneratorDrivenPropertyChecks {
  private val oneHundredGigabytes = oneHundredMegabytes * 1024
  private val fiveMegabytes: Long = 1024 * 1024 * 5
  private val reasonableVideoSize = Gen.choose(fiveMegabytes, oneHundredGigabytes)

  test("chunking") {
    forAll(reasonableVideoSize) { (n: Long) =>
      val chunks = calculateChunks(n)
      val sizes = chunks.map { case (start, end) => end - start }

      sizes.reverse match {
        case Nil =>
          fail("Must have at least one chunk")

        case last :: rest =>
          last must be <= oneHundredMegabytes
          rest.foreach { chunk =>
            chunk must be(oneHundredMegabytes)
          }
      }
    }
  }

  test("chunks follow each other") {
    forAll(reasonableVideoSize) { (n: Long) =>
      val chunks = calculateChunks(n)

      if(chunks.size > 1) {
        chunks.drop(1).foldLeft(chunks.head) { (last, chunk) =>
          val (_, end) = last
          val (start, _) = chunk

          end must be(start)
          chunk
        }
      }

      val (_, end) = chunks.last
      end must be(n)
    }
  }
}
