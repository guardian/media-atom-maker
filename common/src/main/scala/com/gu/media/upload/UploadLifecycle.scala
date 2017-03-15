package com.gu.media.upload

import java.util.UUID

import com.amazonaws.services.s3.model.{CompleteMultipartUploadRequest, CopyPartRequest, InitiateMultipartUploadRequest, PartETag}
import com.gu.media.aws._
import com.gu.media.logging.Logging
import scala.collection.JavaConverters._

class UploadLifecycle(aws: AwsAccess with S3Access with DynamoAccess with KinesisAccess with UploadAccess) extends Logging {
  private val credentials = new CredentialsGenerator(aws)

  def create(metadata: UploadMetadata, size: Long): Upload = {
    val id = UUID.randomUUID().toString
    val parts = chunk(id, size)

    Upload(id, parts, metadata, error = None)
  }

  def credentialsForPart(upload: Upload, part: UploadPart): UploadCredentials = {
    credentials.forKey(upload.id, part.key)
  }

  def partComplete(upload: Upload, part: UploadPart): Upload = {
    val complete = upload.withPart(part.key)(_.copy(uploadedToS3 = true))

    // Put on Kinesis to be picked up by the UploadActionsLambda
    val action = PartUploaded(upload.id, part.key)
    aws.sendOnKinesis(aws.uploadActionsStreamName, upload.id, action)

    if(complete.parts.forall(_.uploadedToS3)) {
      val fullKey = createFullObject(complete).toString
      val action = FullKeyCreated(upload.id, fullKey, upload.parts.map(_.key))

      aws.sendOnKinesis(aws.uploadActionsStreamName, upload.id, action)
      sendToPluto(fullKey, upload.metadata)
    }

    complete
  }

  private def chunk(uploadId: String, size: Long): List[UploadPart] = {
    val boundaries = Upload.calculateChunks(size)

    boundaries.zipWithIndex.map { case ((start, end), id) =>
      UploadPart(UploadPartKey(aws.userUploadFolder, uploadId, id).toString, start, end)
    }
  }

  private def createFullObject(upload: Upload): UploadFullKey = {
    val fullKey = UploadFullKey(aws.userUploadFolder, upload.id)
    val start = new InitiateMultipartUploadRequest(aws.userUploadBucket, fullKey.toString)
    log.info(s"Starting multipart copy for upload ${upload.id}")

    val multipart = aws.s3Client.initiateMultipartUpload(start)
    log.info(s"Started. upload=${upload.id} multipart=${multipart.getUploadId}")

    val eTags = for(part <- upload.parts.indices)
      yield copyPart(multipart.getUploadId, upload.id, part, fullKey.toString)

    val complete = new CompleteMultipartUploadRequest(
      aws.userUploadBucket, fullKey.toString, multipart.getUploadId, eTags.asJava)

    aws.s3Client.completeMultipartUpload(complete)
    log.info(s"Multipart copy complete. upload=${upload.id} multipart=${multipart.getUploadId}")

    fullKey
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

  private def sendToPluto(s3Key: String, metadata: UploadMetadata): Unit = {
    // TODO: send to pluto
  }
}
