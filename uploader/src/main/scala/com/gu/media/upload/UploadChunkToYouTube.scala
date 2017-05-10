package com.gu.media.upload

import com.gu.media.UploadLambda
import com.gu.media.aws.{DynamoAccess, S3Access}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload
import com.gu.media.youtube.{YouTubeAccess, YouTubeUploader}

class UploadChunkToYouTube extends LambdaWithParams[Upload, Upload]
  with Logging
  with YouTubeAccess
  with S3Access
  with DynamoAccess
  with UploadLambda
{
  private val uploader = YouTubeUploader(this, this)
  private val table = new UploadsDataStore(this)

  override def handleUpload(upload: Upload): Upload = {
    val chunk = upload.parts(upload.progress.chunksInS3 - 1)
    val uploadUri = getUploadUri(upload)

    uploader.uploadPart(upload, chunk, uploadUri)

    val updated = upload.copy(
      metadata = upload.metadata.copy(youTubeUploadUri = Some(uploadUri)),
      progress = upload.progress.copy(uploadedToYouTube = chunk.end)
    )

    table.put(updated)
    updated
  }

  private def getUploadUri(upload: Upload): String = upload.metadata.youTubeUploadUri.getOrElse {
    uploader.startUpload(upload.metadata.title, upload.metadata.channel, upload.id, upload.parts.last.end)
  }
}
