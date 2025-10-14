package com.gu.media.upload

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UploadKeyTest extends AnyFlatSpec with Matchers {

  "UploadKey" should "link a path and filename" in {
    UploadKey("my-path", "my-file").toString shouldBe "my-path/my-file"
    UploadKey(
      "my-path/with/slashes",
      "my-file"
    ).toString shouldBe "my-path/with/slashes/my-file"
  }

  "UploadUri" should "create an s3:// uri" in {
    UploadUri("my-bucket", "my-key").toString shouldBe "s3://my-bucket/my-key"
  }

  "UploadPartKey" should "create a numbered key containing the id" in {
    UploadPartKey(
      "my-folder",
      id = "123xyz",
      part = 24
    ).toString shouldBe "my-folder/123xyz/parts/24"
  }

  "CompleteUploadKey" should "create a key called 'complete' containing the id" in {
    CompleteUploadKey(
      "my-folder",
      id = "123xyz"
    ).toString shouldBe "my-folder/123xyz/complete"
  }

  "TranscoderOutputKey" should "combine prefix, title, id and extension" in {
    TranscoderOutputKey(
      "my-prefix",
      "my-title",
      id = "123xyz",
      extension = "m3u8"
    ).toString shouldBe "my-prefix/my-title--123xyz.m3u8"
  }

  "TranscoderOutputKey" should "set the prefix to date yyyy/MM/dd" in {
    val key = TranscoderOutputKey("my-title", id = "123xyz", extension = "m3u8")
    key.toString should fullyMatch regex """\d{4}/\d{2}/\d{2}/my-title--123xyz.m3u8"""
  }

  "TranscoderOutputKey" should "include asset and subtitle versions for the atom" in {
    val key = TranscoderOutputKey(
      "my-title",
      atomId = "123xyz",
      assetVersion = 2,
      subtitleVersion = 10,
      extension = "m3u8"
    )
    key.toString should fullyMatch regex """\d{4}/\d{2}/\d{2}/my-title--123xyz-2.10.m3u8"""
  }

  "TranscoderOutputKey" should "replace special characters with underscores" in {
    TranscoderOutputKey(
      "2025/08/20",
      "Loop: Japan fireball",
      id = "1c44ce4e-760a-4312-a803-40939aeea355-2.0",
      extension = "m3u8"
    ).toString shouldBe "2025/08/20/Loop__Japan_fireball--1c44ce4e-760a-4312-a803-40939aeea355-2.0.m3u8"
    TranscoderOutputKey(
      "my prefix",
      "!@£$%^&*()",
      id = "123xyz",
      extension = "m3u8"
    ).toString shouldBe "my_prefix/__________--123xyz.m3u8"
  }

  "TranscoderOutputKey.stripSpecialCharsInPath" should "replace special characters with underscore but preserve slashes" in {
    TranscoderOutputKey.stripSpecialCharsInPath(
      "2025/08/20/11:59:59.123"
    ) should be("2025/08/20/11_59_59_123")
  }

  "TranscoderOutputKey.stripSpecialCharsInFilename" should "replace special characters with underscore but preserve dots" in {
    TranscoderOutputKey.stripSpecialCharsInFilename(
      "!@£$%^&*()/--1c44ce4e-760a-4312-a803-40939aeea355-2.0.m3u8"
    ) should be(
      "___________--1c44ce4e-760a-4312-a803-40939aeea355-2.0.m3u8"
    )
  }
}
