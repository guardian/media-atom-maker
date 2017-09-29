package model

import com.gu.media.model.{SelfHostedAsset, VideoSource, YouTubeAsset}
import com.gu.media.upload.model._
import com.gu.media.youtube.YouTubeProcessingStatus
import org.scalatest.{FunSuite, MustMatchers}

class ClientAssetTest extends FunSuite with MustMatchers {
  val ytAsset = Asset(AssetType.Video, 1, "12345", Platform.Youtube, None)
  val ytProcessing = YouTubeProcessingStatus("1", "processing", 0, 0, 0, None)

  val mp4 = Asset(AssetType.Video, 1, "test.mp4", Platform.Url, Some("video/mp4"))
  val m3u8 = Asset(AssetType.Video, 1, "test.m3u8", Platform.Url, Some("video/m3u8"))
  val selfHostedAsset = SelfHostedAsset(List(
    VideoSource(mp4.id, mp4.mimeType.get), VideoSource(m3u8.id, m3u8.mimeType.get)
  ))

  val parts = List(UploadPart("1", 0, 10), UploadPart("2", 10, 20))

  test("Group assets by version (newest first)") {
    val assets = List(mp4.copy(version = 1), m3u8.copy(version = 1), ytAsset.copy(version = 2))
    val output = ClientAsset.fromAssets(assets)

    output match {
      case first :: second :: Nil =>
        first.asset.get mustBe a[YouTubeAsset]
        second.asset.get mustBe a[SelfHostedAsset]

      case _ =>
        fail(s"Expected two entries, got $output")
    }
  }

  test("self hosted asset") {
    val (version, actual) = ClientAsset.videoFromAssets(List(mp4, m3u8))

    version must be(1)
    actual must be(selfHostedAsset)
  }

  test("YouTube processing (indeterminate)") {
    val status = YouTubeProcessingStatus("", "processing", 0, 0, 0, None)
    val expected = ClientAssetProcessing("YouTube Processing", failed = false, None, None)
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  test("YouTube processing (time left)") {
    val status = YouTubeProcessingStatus("", "processing", 10, 5, 10000, None)
    val expected = ClientAssetProcessing("YouTube Processing (10s left)", failed = false, Some(5), Some(10))
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  test("YouTube processing (error)") {
    val status = YouTubeProcessingStatus("", "failed", 0, 0, 0, Some("burp"))
    val expected = ClientAssetProcessing("burp", failed = true, None, None)
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  test("YouTube processing (something else)") {
    val status = YouTubeProcessingStatus("", "reticulating", 0, 0, 0, None)
    val expected = ClientAssetProcessing("reticulating", failed = false, None, None)
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  test("Upload with original filename") {
    val progress = UploadProgress(chunksInS3 = 2, chunksInYouTube = 2, fullyUploaded = true, fullyTranscoded = false, retries = 0)
    val metadata = blank(selfHost = false).copy(originalFilename = Some("test.mp4"))
    val upload = Upload("test", parts, metadata, progress)

    val actual = ClientAsset.fromUpload("test", upload, error = None)
    actual.originalFilename must contain("test.mp4")
  }

  private def blank(selfHost: Boolean): UploadMetadata = {
    UploadMetadata("", "", "", "", null, selfHost, null, None)
  }
}
