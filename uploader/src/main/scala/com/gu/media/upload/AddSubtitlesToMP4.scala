package com.gu.media.upload

import com.gu.media.aws.{MediaConvertAccess, S3Access}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.VideoSource.mimeTypeMp4
import com.gu.media.model.SelfHostedInput
import com.gu.media.upload.model.Upload
import software.amazon.awssdk.services.s3.model.{
  GetObjectRequest,
  PutObjectRequest
}
import java.nio.file.{Files, Path}
import scala.util.Random

class AddSubtitlesToMP4
    extends LambdaWithParams[Upload, Upload]
    with S3Access
    with MediaConvertAccess
    with Logging {

  private val tmpdir = Files.createTempDirectory("media-atom-uploader")

  def createTempPath(
      prefix: String,
      suffix: String,
      dir: Path = tmpdir
  ): Path = {
    val n = Random.nextLong()
    val s = prefix + java.lang.Long.toUnsignedString(n) + suffix
    dir.resolve(s)
  }

  private def s3Download(bucket: String, key: String, path: Path) = {
    val getObjectRequest = GetObjectRequest.builder
      .bucket(bucket)
      .key(key)
      .build
    val obj = s3Client.getObject(getObjectRequest)
    Files.copy(obj, path)
  }

  private def s3Upload(bucket: String, key: String, path: Path) = {
    val putObjectRequest = PutObjectRequest.builder
      .bucket(bucket)
      .key(key)
      .build
    s3Client.putObject(putObjectRequest, path)
  }

  override def handle(upload: Upload): Upload = {
    for {
      videoSources <- upload.metadata.asset
        .collect({ case asset: SelfHostedInput =>
          asset.sources
        })
        .toList
      videoSource <- videoSources if videoSource.mimeType == mimeTypeMp4
      subtitleSource <- upload.metadata.subtitleSource
    } yield {
      val subtitlesFile = createTempPath("input-subtitles-", ".srt")
      val videoFile = createTempPath("input-video-", ".mp4")
      val updatedVideo = createTempPath("output-video-", ".mp4")

      s3Download(destinationBucket, videoSource.src, videoFile)
      s3Download(upload.metadata.bucket, subtitleSource.src, subtitlesFile)
      FfMpeg.addSubtitlesToMP4(videoFile, subtitlesFile, updatedVideo)
      s3Upload(destinationBucket, videoSource.src, updatedVideo)
    }
    upload
  }
}
