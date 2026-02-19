package com.gu.media.upload

import com.gu.media.model.{PlutoSyncMetadataMessage, VideoInput}
import com.gu.media.upload.model.{
  SelfHostedUploadMetadata,
  Upload,
  UploadMetadata,
  UploadProgress
}
import com.gu.media.upload.model.Upload._
import org.scalacheck.Gen
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class UploadTest
    extends AnyFunSuite
    with Matchers
    with ScalaCheckDrivenPropertyChecks {
  private val oneHundredGigabytes = oneHundredMegabytes * 1024
  private val fiveMegabytes: Long = 1024 * 1024 * 5
  private val reasonableVideoSize =
    Gen.choose(fiveMegabytes, oneHundredGigabytes)

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

      if (chunks.size > 1) {
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

  test("videoInputUri returns s3 uri from upload metadata") {
    val upload = Upload("123xyz", Nil, metadataWithoutSubtitle, progress)

    Upload
      .videoInputUri(upload)
      .toString mustBe "s3://upload-bucket/uploads/123xyz-1/complete"
  }

  test(
    "subtitleInputUri returns s3 uri from upload metadata if there is a subtitleSource"
  ) {
    val upload = Upload("123xyz", Nil, metadataWithSubtitle, progress)

    Upload.subtitleInputUri(upload).map(_.toString) must contain(
      "s3://upload-bucket/uploads/123xyz-1/subtitles.srt"
    )
  }

  test("subtitleInputUri returns None if there is no subtitleSource") {
    val upload = Upload("123xyz", Nil, metadataWithoutSubtitle, progress)

    Upload.subtitleInputUri(upload).map(_.toString) mustBe empty
  }

  test("getCurrentSubtitleVersion return stored version or zero as default") {
    val upload1 = Upload("123xyz", Nil, metadataWithSubtitle, progress)
    Upload.getCurrentSubtitleVersion(upload1) mustBe 12L
    val upload2 = Upload("123xyz", Nil, metadataWithoutSubtitle, progress)
    Upload.getCurrentSubtitleVersion(upload2) mustBe 0L
  }

  test("getNextSubtitleVersion return stored version + 1 or one as default") {
    val upload1 = Upload("123xyz", Nil, metadataWithSubtitle, progress)
    Upload.getNextSubtitleVersion(upload1) mustBe 13L
    val upload2 = Upload("123xyz", Nil, metadataWithoutSubtitle, progress)
    Upload.getNextSubtitleVersion(upload2) mustBe 1L
  }

  private def plutoMessage = PlutoSyncMetadataMessage(
    "video-upload",
    projectId = Some("project"),
    s3Key = "uploads/123xyz-1/complete",
    atomId = "123xyz",
    title = "my-video",
    user = "me",
    posterImageUrl = None
  )

  private def progress =
    UploadProgress(4, 0, fullyUploaded = true, fullyTranscoded = true, 0, None)

  private def metadataWithSubtitle = UploadMetadata(
    user = "me",
    bucket = "upload-bucket",
    region = "local",
    title = "my-video",
    pluto = plutoMessage,
    iconikData = None,
    runtime = SelfHostedUploadMetadata(Nil),
    subtitleVersion = Some(12),
    subtitleSource =
      Some(VideoInput("uploads/123xyz-1/subtitles.srt", "video/mp4"))
  )

  private def metadataWithoutSubtitle = UploadMetadata(
    user = "me",
    bucket = "upload-bucket",
    region = "local",
    title = "my-video",
    pluto = plutoMessage,
    iconikData = None,
    runtime = SelfHostedUploadMetadata(Nil)
  )
}
