package model

import com.gu.media.model._
import com.gu.media.upload.model._
import com.gu.media.youtube.YouTubeProcessingStatus
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class ClientAssetTest extends AnyFunSuite with Matchers {
  val ytAsset =
    Asset(AssetType.Video, 1, "12345", Platform.Youtube, None, None, None)
  val ytProcessing = YouTubeProcessingStatus("1", "processing", 0, 0, 0, None)

  val mp4 =
    Asset(
      AssetType.Video,
      1,
      "test.mp4",
      Platform.Url,
      Some("video/mp4"),
      Some(ImageAssetDimensions(1280, 720)),
      Some("16:9")
    )
  val m3u8 =
    Asset(
      AssetType.Video,
      1,
      "test.m3u8",
      Platform.Url,
      Some("application/vnd.apple.mpegurl"),
      Some(ImageAssetDimensions(1280, 720)),
      Some("16:9")
    )
  val selfHostedAsset = SelfHostedAsset(
    List(
      VideoSource(mp4.id, mp4.mimeType.get, Some(1280), Some(720)),
      VideoSource(m3u8.id, m3u8.mimeType.get, Some(1280), Some(720))
    )
  )

  val parts = List(UploadPart("1", 0, 10), UploadPart("2", 10, 20))

  test("Group assets by version (latest first)") {
    val assets = List(
      mp4.copy(version = 1),
      m3u8.copy(version = 1),
      ytAsset.copy(version = 2)
    )
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
    val expected =
      ClientAssetProcessing("YouTube Processing", failed = false, None, None)
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  test("YouTube processing (time left)") {
    val status = YouTubeProcessingStatus("", "processing", 10, 5, 10000, None)
    val expected = ClientAssetProcessing(
      "YouTube Processing (10s left)",
      failed = false,
      Some(5),
      Some(10)
    )
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
    val expected =
      ClientAssetProcessing("reticulating", failed = false, None, None)
    val actual = ClientAssetProcessing(status)

    actual must be(expected)
  }

  test("Upload with original filename") {
    val progress = UploadProgress(
      chunksInS3 = 2,
      chunksInYouTube = 2,
      fullyUploaded = true,
      fullyTranscoded = false,
      retries = 0
    )
    val metadata = UploadMetadata(
      "",
      "",
      "",
      "",
      null,
      iconikData = None,
      null,
      originalFilename = Some("test.mp4")
    )
    val upload = Upload("test", parts, metadata, progress)

    val actual = ClientAsset.fromUpload("test", 1234, upload, error = None)
    actual.metadata.get.originalFilename must contain("test.mp4")
  }

  test("convert client asset id back to version number") {
    ClientAsset.getVersion(ClientAsset("1")) mustBe 1L
    ClientAsset.getVersion(ClientAsset("10")) mustBe 10L
    ClientAsset.getVersion(ClientAsset("foo")) mustBe 0L
  }
}
