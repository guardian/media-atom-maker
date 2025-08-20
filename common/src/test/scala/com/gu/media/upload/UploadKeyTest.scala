package com.gu.media.upload

import org.scalatest.Matchers.regex
import org.scalatest.{FlatSpec, Matchers}

import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}

class UploadKeyTest extends FlatSpec with Matchers {

  "UploadKey" should "link a path and filename" in {
    UploadKey("my-path", "my-file").toString shouldBe "my-path/my-file"
    UploadKey("my-path/with/slashes", "my-file").toString shouldBe "my-path/with/slashes/my-file"
  }

  "UploadUri" should "create an s3:// uri" in {
    UploadUri("my-bucket", "my-key").toString shouldBe "s3://my-bucket/my-key"
  }

  "UploadPartKey" should "create a numbered key containing the id" in {
    UploadPartKey("my-folder", id = "123xyz", part = 24).toString shouldBe "my-folder/123xyz/parts/24"
  }

  "CompleteUploadKey" should "create a key called 'complete' containing the id" in {
    CompleteUploadKey("my-folder", id = "123xyz").toString shouldBe "my-folder/123xyz/complete"
  }

  "TranscoderOutputKey" should "combine prefix, title, id and extension" in {
    TranscoderOutputKey("my-prefix", "my-title", id = "123xyz", extension = "m3u8")
      .toString shouldBe "my-prefix/my-title--123xyz.m3u8"
  }

  "TranscoderOutputKey" should "set the prefix to date yyyy/MM/dd" in {
    val key = TranscoderOutputKey("my-title", id = "123xyz", extension = "m3u8")
    key.toString should fullyMatch regex """\d{4}/\d{2}/\d{2}/my-title--123xyz.m3u8"""
  }

  "TranscoderOutputKey" should "include major and minor versions for the atom" in {
    val key = TranscoderOutputKey("my-title", atomId = "123xyz", version = 2, subtitleVersion = 10, extension = "m3u8")
    key.toString should fullyMatch regex """\d{4}/\d{2}/\d{2}/my-title--123xyz-2.10.m3u8"""
  }
}
