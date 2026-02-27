package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, S3Access}
import com.gu.media.lambda.{
  LambdaBase,
  LambdaWithParams,
  LambdaYoutubeCredentials
}
import com.gu.media.logging.Logging
import com.gu.media.upload.model.{Upload, YouTubeUploadMetadata}
import com.gu.media.youtube.{YouTubeAccess, YouTubeUploader}

class UploadChunkToYouTube
    extends LambdaWithParams[Upload, Upload]
    with LambdaBase
    with Logging
    with YouTubeAccess
    with LambdaYoutubeCredentials
    with S3Access
    with DynamoAccess {
  private val uploader = new YouTubeUploader(this, this.s3Client)

  override def handle(upload: Upload): Upload = {
    val chunk = upload.parts(upload.progress.chunksInS3 - 1)
    val (uploadUri, runtimeMetadata) = getUploadUri(upload)

    val after = uploader.uploadPart(upload, chunk, uploadUri)

    val updated = after.copy(
      metadata = after.metadata.copy(runtime = runtimeMetadata),
      progress = after.progress.copy(chunksInYouTube =
        upload.progress.chunksInYouTube + 1
      )
    )

    updated
  }

  private def getUploadUri(upload: Upload): (String, YouTubeUploadMetadata) =
    upload.metadata.runtime match {
      case meta @ YouTubeUploadMetadata(_, Some(uri)) =>
        (uri, meta)

      case YouTubeUploadMetadata(channel, None) =>
        val uri = uploader.startUpload(
          upload.metadata.title,
          channel,
          upload.id,
          upload.parts.last.end
        )
        (uri, YouTubeUploadMetadata(channel, Some(uri)))

      case other =>
        throw new IllegalStateException(s"Unexpected runtime metadata $other")
    }
}
