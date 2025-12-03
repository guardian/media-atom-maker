package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, S3Access}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.upload.model.{Upload, UploadPart}
import software.amazon.awssdk.services.s3.model.{HeadObjectRequest, S3Exception}

class GetChunkFromS3
    extends LambdaWithParams[Upload, Upload]
    with S3Access
    with DynamoAccess {
  override def handle(upload: Upload): Upload = {
    val chunk = upload.parts(upload.progress.chunksInS3)

    def objectExists(bucket: String, key: String): Boolean = try {
      s3Client.headObject(HeadObjectRequest.builder.bucket(bucket).key(key).build)
      true
    } catch {
      case e: S3Exception =>
        if (e.statusCode == 404) return false
        throw e
    }

    val updated =
      if (objectExists(upload.metadata.bucket, chunk.key)) {
        complete(upload, chunk)
      } else {
        retry(upload, chunk)
      }

    updated
  }

  private def complete(upload: Upload, chunk: UploadPart): Upload = {
    upload.copy(progress =
      upload.progress.copy(
        fullyUploaded = chunk == upload.parts.last,
        chunksInS3 = upload.progress.chunksInS3 + 1,
        retries = 0
      )
    )
  }

  private def retry(upload: Upload, chunk: UploadPart): Upload = {
    upload.copy(progress =
      upload.progress.copy(
        fullyUploaded = false,
        retries = upload.progress.retries + 1
      )
    )
  }
}
