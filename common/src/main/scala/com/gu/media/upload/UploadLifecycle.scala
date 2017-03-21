package com.gu.media.upload

import java.util.UUID

import com.amazonaws.services.s3.model.{CompleteMultipartUploadRequest, CopyPartRequest, InitiateMultipartUploadRequest, PartETag}
import com.gu.media.aws._
import com.gu.media.logging.Logging
import com.gu.media.upload.UploadLifecycle.UploadAws
import com.gu.media.upload.actions.{DeleteParts, UploadActionSender, UploadPartToYouTube}

import scala.collection.JavaConverters._

class UploadLifecycle(aws: UploadAws, actions: UploadActionSender) extends Logging {
  private val credentials = new CredentialsGenerator(aws)

  def create(metadata: UploadMetadata, youtubeMetadata: YouTubeMetadata, size: Long): Upload = {
    val id = UUID.randomUUID().toString
    val parts = chunk(id, size)

    Upload(id, parts, metadata, youtubeMetadata)
  }

  def credentialsForPart(upload: Upload, part: UploadPart): UploadCredentials = {
    credentials.forKey(upload.id, part.key)
  }

  def partComplete(upload: Upload, part: UploadPart): Upload = {
    val complete = upload.withPart(part.key)(_.copy(uploadedToS3 = true))

    actions.send(upload.id, UploadPartToYouTube(upload.id, part.key))

    if(complete.parts.forall(_.uploadedToS3)) {
      val completeKey = createCompleteObject(complete).toString
      val action = DeleteParts(upload.id, upload.parts.map(_.key))

      actions.send(upload.id, action)
    }

    complete
  }

  private def chunk(uploadId: String, size: Long): List[UploadPart] = {
    val boundaries = Upload.calculateChunks(size)

    boundaries.zipWithIndex.map { case ((start, end), id) =>
      UploadPart(UploadPartKey(aws.userUploadFolder, uploadId, id).toString, start, end)
    }
  }

  // Videos are uploaded as a series of smaller parts. In order to simplify Pluto ingestion and allow us
  // to transcode if required, we use S3 multipart copy to create a new object consisting of all the parts
  private def createCompleteObject(upload: Upload): CompleteUploadKey = {
    val completeKey = CompleteUploadKey(aws.userUploadFolder, upload.id)
    val start = new InitiateMultipartUploadRequest(aws.userUploadBucket, completeKey.toString)
    log.info(s"Starting multipart copy for upload ${upload.id}")

    val multipart = aws.s3Client.initiateMultipartUpload(start)
    log.info(s"Started. upload=${upload.id} multipart=${multipart.getUploadId}")

    val eTags = for(part <- upload.parts.indices)
      yield copyPart(multipart.getUploadId, upload.id, part, completeKey.toString)

    val complete = new CompleteMultipartUploadRequest(
      aws.userUploadBucket, completeKey.toString, multipart.getUploadId, eTags.asJava)

    aws.s3Client.completeMultipartUpload(complete)
    log.info(s"Multipart copy complete. upload=${upload.id} multipart=${multipart.getUploadId}")

    completeKey
  }

  private def copyPart(multipartId: String, uploadId: String, part: Int, key: String): PartETag = {
    val request = new CopyPartRequest()
      .withUploadId(multipartId)
      .withSourceBucketName(aws.userUploadBucket)
      .withSourceKey(UploadPartKey(aws.userUploadFolder, uploadId, part).toString)
      .withDestinationBucketName(aws.userUploadBucket)
      .withDestinationKey(key.toString)
      .withPartNumber(part + 1)

    log.info(s"Copying upload=$uploadId multipart=$multipartId part=$part")
    val response = aws.s3Client.copyPart(request)

    new PartETag(response.getPartNumber, response.getETag)
  }
}

object UploadLifecycle {
  type UploadAws = AwsAccess with S3Access with DynamoAccess with KinesisAccess with UploadAccess
}
