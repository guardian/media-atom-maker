package com.gu.media.upload

import org.scalatest.{FunSuite, MustMatchers}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import Upload.{calculateChunks, oneHundredMegabytes, twoFiveSixKilobytes}
import org.scalacheck.Gen

class UploadTest extends FunSuite with MustMatchers with GeneratorDrivenPropertyChecks {
  private val oneHundredGigabytes = oneHundredMegabytes * 1024
  private val reasonableVideoSize = Gen.choose(twoFiveSixKilobytes, oneHundredGigabytes)

  test("256kb") {
    val (start, end) :: Nil = calculateChunks(twoFiveSixKilobytes)

    start must be(0)
    end must be(twoFiveSixKilobytes)
  }

  test("large enough to chunk but last chunk is multiple of 256kb") {
    val first :: second :: Nil = calculateChunks(oneHundredMegabytes + twoFiveSixKilobytes)

    first must be((0, oneHundredMegabytes))
    second must be(oneHundredMegabytes, oneHundredMegabytes + twoFiveSixKilobytes)
  }

  test("exact multiple of 100MB") {
    val first :: second :: Nil = calculateChunks(oneHundredMegabytes * 2)

    first must be((0, oneHundredMegabytes))
    second must be(oneHundredMegabytes, oneHundredMegabytes * 2)
  }

  test("chunking") {
    // We want 100MB chunks. YouTube mandates that chunk size must be a multiple of 256KB
    // As such:
    //   - the last chunk can be any size (but less than the preceding chunk)
    //   - the penultimate chunk should always mod 256KB
    //   - any other chunks should be exactly 100MB
    forAll(reasonableVideoSize) { (n: Long) =>
      val chunks = calculateChunks(n)
      val backwards = chunks.reverse

      backwards match {
        case Nil =>
          fail("Empty chunk list")

        case (start, end) :: Nil =>
          start must be(0)
          end must be(oneHundredMegabytes)

        case (lastStart, lastEnd) :: (littleStart, littleEnd) :: rest =>
          lastEnd must be(n)
          littleEnd must be(lastStart)

          ((littleEnd - littleStart) % twoFiveSixKilobytes) must be(0)

          rest.foldLeft(littleStart) { case (previousStart, (start, end)) =>
            end must be(previousStart)
            (end - start) must be(oneHundredMegabytes)

            start
          }
      }
    }
  }
}
