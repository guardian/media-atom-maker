package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, S3Access}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.{Upload, YouTubeUploadMetadata}
import com.gu.media.youtube.{YouTubeAccess, YouTubeUploader}

class UploadChunkToYouTube extends LambdaWithParams[Upload, Upload]
  with Logging
  with YouTubeAccess
  with S3Access
  with DynamoAccess
{
  private val uploader = new YouTubeUploader(this, this.s3Client)

  override def handle(upload: Upload): Upload = {
    val chunk = upload.parts(upload.progress.chunksInS3 - 1)
    val uploadUri = getUploadUri(upload)

    val after = uploader.uploadPart(upload, chunk, uploadUri)

    val updated = after.copy(
      metadata = after.metadata.copy(runtime = Some(YouTubeUploadMetadata(uploadUri))),
      progress = after.progress.copy(chunksInYouTube = upload.progress.chunksInYouTube + 1)
    )

    updated
  }

  private def getUploadUri(upload: Upload): String = upload.metadata.runtime match {
    case Some(YouTubeUploadMetadata(uri)) => uri
    case Some(other) => throw new IllegalStateException(s"Unexpected runtime metadata $other")
    case None => uploader.startUpload(upload.metadata.title, upload.metadata.channel, upload.id, upload.parts.last.end)
  }
}
