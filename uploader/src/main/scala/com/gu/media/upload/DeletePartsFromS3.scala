package com.gu.media.upload

import com.gu.media.aws.S3Access
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

import scala.util.control.NonFatal

class DeletePartsFromS3 extends LambdaWithParams[Upload, Upload] with S3Access with Logging {
  override def handle(upload: Upload) = {
    // The complete key will be deleted once it has been ingested by Pluto
    upload.parts.foreach { part =>
      try {
        log.info(s"Deleting part $part")
        s3Client.deleteObject(upload.metadata.bucket, part.key)
      } catch {
        case NonFatal(err) =>
          // if we can't delete it, no problem. the bucket policy will remove it in time
          log.warn(s"Unable to delete part $part: $err")
      }
    }
  }
}
