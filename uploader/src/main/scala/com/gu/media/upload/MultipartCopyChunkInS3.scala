package com.gu.media.upload


import com.gu.media.aws.S3Access
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.{CopyETag, CopyProgress, Upload}
import software.amazon.awssdk.services.s3.model.{CreateMultipartUploadRequest, UploadPartCopyRequest}

class MultipartCopyChunkInS3
    extends LambdaWithParams[Upload, Upload]
    with S3Access
    with Logging {
  override def handle(upload: Upload): Upload = {
    upload.copy(
      progress = upload.progress.copy(
        copyProgress = Some(upload.progress.copyProgress match {
          case Some(before) => uploadPart(upload, before)
          case None         => start(upload)
        })
      )
    )
  }

  private def start(upload: Upload): CopyProgress = {
    log.info(s"Starting multipart copy for upload ${upload.id}")

    val start = CreateMultipartUploadRequest.builder()
                .bucket(upload.metadata.bucket).key(upload.metadata.pluto.s3Key).build()

    val copyId = s3Client.createMultipartUpload(start).uploadId()

    CopyProgress(copyId, fullyCopied = false, eTags = List.empty)
  }

  private def uploadPart(
      upload: Upload,
      progress: CopyProgress
  ): CopyProgress = {
    val part = progress.eTags.size

    if (part >= upload.parts.size) {
      log.error(
        "Invoked CreateCompleteVideoInS3 handler even though all parts are uploaded"
      )
      progress.copy(fullyCopied = true)
    } else {
      val bucket = upload.metadata.bucket
      val source = upload.parts(part).key
      val destination = upload.metadata.pluto.s3Key

      val request =  UploadPartCopyRequest.builder()
        .uploadId(progress.copyId)
        .sourceBucket(bucket)
        .sourceKey(source)
        .destinationBucket(bucket)
        .destinationBucket(destination)
        .partNumber(part + 1)
        .build()

      log.info(
        s"Copying $source to $destination [multipart=${progress.copyId} part=$part]"
      )

      val response = s3Client.uploadPartCopy(request)
      val eTags =
        progress.eTags :+ CopyETag(request.partNumber(), response.copyPartResult().eTag())

      progress.copy(
        eTags = eTags,
        fullyCopied = eTags.size == upload.parts.size
      )
    }
  }
}
