package com.gu.media.upload

import com.gu.media.aws.S3Access
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.upload.model.{Upload, UploadPart}

class GetChunkFromS3 extends LambdaWithParams[Upload, Upload] with S3Access {
  override def handle(upload: Upload): Upload = {
    val chunk = upload.parts(upload.progress.chunksInS3)

    if(s3Client.doesObjectExist(upload.metadata.bucket, chunk.key)) {
      complete(upload, chunk)
    } else {
      retry(upload, chunk)
    }
  }

  private def complete(upload: Upload, chunk: UploadPart): Upload = {
    upload.copy(progress = upload.progress.copy(
      fullyUploaded = chunk == upload.parts.last,
      uploadedToS3 = chunk.end,
      chunksInS3 = upload.progress.chunksInS3 + 1,
      retries = 0
    ))
  }

  private def retry(upload: Upload, chunk: UploadPart): Upload = {
    upload.copy(progress = upload.progress.copy(
      fullyUploaded = false,
      retries = upload.progress.retries + 1
    ))
  }
}
