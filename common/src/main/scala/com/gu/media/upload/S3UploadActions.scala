package com.gu.media.upload

import java.io.InputStream

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import com.gu.media.logging.Logging
import com.gu.media.upload.model.{Upload, UploadPart}

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

class S3UploadActions(s3: AmazonS3Client) extends Logging {
  // Videos are uploaded as a series of smaller parts. In order to simplify Pluto ingestion and allow us
  // to transcode if required, we use S3 multipart copy to create a new object consisting of all the parts
  def createCompleteObject(upload: Upload, destination: String): Unit = {
    val bucket = upload.metadata.bucket
    val parts = upload.parts.map { case UploadPart(key, _, _) => key }

    if(parts.forall(objectExists(bucket, _))) {
      val start = new InitiateMultipartUploadRequest(bucket, destination)
      log.info(s"Starting multipart copy for upload ${upload.id}")

      val multipart = s3.initiateMultipartUpload(start)
      val eTags = parts.zipWithIndex.map { case(key, part) =>
        copyPart(multipart.getUploadId, bucket, part, key, destination)
      }

      val complete = new CompleteMultipartUploadRequest(
        bucket, destination, multipart.getUploadId, eTags.asJava)

      s3.completeMultipartUpload(complete)

      log.info(s"Multipart copy complete. upload=${upload.id} multipart=${multipart.getUploadId}")
    } else {
      throw new IllegalStateException(s"Unable to create complete object $destination since the parts have been deleted from S3")
    }
  }

  // TODO MRB: remove this once moved to step functions
  def objectExists(bucket: String, key: String) = try {
    s3.doesObjectExist(bucket, key)
  } catch {
    case e: AmazonS3Exception =>
      log.error(s"Error checking $key", e)
      false
  }

  def getObjectInput(bucket: String, key: String): Option[InputStream] = {
    if(objectExists(bucket, key)) {
      Some(s3.getObject(bucket, key).getObjectContent)
    } else {
      None
    }
  }

  def deleteParts(upload: Upload): Unit = {
    // The complete key will be deleted once it has been ingested by Pluto
    upload.parts.foreach { part =>
      try {
        log.info(s"Deleting part $part")
        s3.deleteObject(upload.metadata.bucket, part.key)
      } catch {
        case NonFatal(err) =>
          // if we can't delete it, no problem. the bucket policy will remove it in time
          log.warn(s"Unable to delete part $part: $err")
      }
    }
  }

  private def copyPart(multipartId: String, bucket: String, part: Int, source: String, destination: String): PartETag = {
    val request = new CopyPartRequest()
      .withUploadId(multipartId)
      .withSourceBucketName(bucket)
      .withSourceKey(source)
      .withDestinationBucketName(bucket)
      .withDestinationKey(destination)
      .withPartNumber(part + 1)

    log.info(s"Copying $source to $destination [multipart=$multipartId part=$part]")
    val response = s3.copyPart(request)

    new PartETag(response.getPartNumber, response.getETag)
  }
}
