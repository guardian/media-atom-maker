package com.gu.media.upload


import software.amazon.awssdk.services.s3.model.{CompleteMultipartUploadRequest, CompletedMultipartUpload, CompletedPart, DeleteObjectRequest}
import com.gu.media.aws.S3Access
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.{CopyETag, CopyProgress, Upload}

import scala.util.control.NonFatal
import scala.jdk.CollectionConverters._

class CompleteMultipartCopy
    extends LambdaWithParams[Upload, Upload]
    with S3Access
    with Logging {
  override def handle(upload: Upload) = {
    upload.progress.copyProgress match {
      case Some(CopyProgress(copyId, _, copyETags)) =>
        val bucket = upload.metadata.bucket
        val destination = upload.metadata.pluto.s3Key
        val completedParts = copyETags.map { case CopyETag(partNumber, eTag) =>
          CompletedPart.builder()
            .partNumber(partNumber)
            .eTag(eTag)
            .build()
        }
        val  completedUpload = CompletedMultipartUpload.builder()
          .parts(completedParts.asJava)
          .build()
        log.info(
          s"Completing multipart copy. upload=${upload.id} multipart=$copyId"
        )
        val request = CompleteMultipartUploadRequest.builder()
                      .bucket(bucket)
                      .key(destination)
                      .uploadId(copyId)
                      .multipartUpload(completedUpload)
                      .build()

        s3Client.completeMultipartUpload(request)

      case None =>
        log.error("Missing copyProgress when invoking CompleteMultipartCopy")
    }

    // The complete key will be deleted once it has been ingested by Pluto
    upload.parts.foreach { part =>
      try {
        log.info(s"Deleting part $part")
        val deleteRequest = DeleteObjectRequest.builder().bucket(upload.metadata.bucket).key(part.key).build()
        s3Client.deleteObject(deleteRequest)
      } catch {
        case NonFatal(err) =>
          // if we can't delete it, no problem. the bucket policy will remove it in time
          log.warn(s"Unable to delete part $part: $err")
      }
    }

    upload
  }
}
