package com.gu.media

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.model.{CompleteMultipartUploadRequest, CopyPartRequest, InitiateMultipartUploadRequest, PartETag}
import com.gu.media.aws._
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.upload.{DynamoUploadsTable, Upload, UploadFullKey, UploadPartKey}

import scala.collection.JavaConverters._

class S3EventsLambda extends RequestHandler[S3Event, Unit]
  with LambdaBase
  with Logging
  with AwsAccess with CrossAccountAccess
  with S3Access
  with DynamoAccess
  with KinesisAccess
  with UploadAccess {

  val table = new DynamoUploadsTable(this)

  override def handleRequest(input: S3Event, context: Context): Unit = {
    val records = input.getRecords.asScala.toList
    val keys = records.map(_.getS3.getObject.getKey)

    getPartKeys(keys).foreach { key =>
      addToStream(key)

      // consistent read here to ensure we've seen all uploaded parts
      table.consistentlyGet(key.id).foreach { upload =>
        val updated = completePart(key, upload)

        if(allPartsInS3(updated)) {
          createFullObject(upload)
        }

        table.put(updated)
      }
    }

    getFullKeys(keys).foreach { fullKey =>
      // TODO: send to pluto
    }
  }

  private def getPartKeys(keys: List[String]): List[UploadPartKey] = {
    keys.flatMap {
      case UploadPartKey(folder, id, part) =>
        log.info(s"Chunk complete. folder=$folder id=$id part=$part")
        Some(UploadPartKey(folder, id, part))

      case other =>
        None
    }
  }

  private def getFullKeys(keys: List[String]): List[UploadFullKey] = {
    keys.flatMap {
      case UploadFullKey(folder, id) =>
        log.info(s"Full key available. folder=$folder id=$id")
        Some(UploadFullKey(folder, id))

      case other =>
        None
    }
  }

  private def createFullObject(upload: Upload): Unit = {
    val fullKey = UploadFullKey(userUploadFolder, upload.id)
    val start = new InitiateMultipartUploadRequest(userUploadBucket, fullKey.toString)
    log.info(s"Starting multipart copy for upload ${upload.id}")

    val multipart = s3Client.initiateMultipartUpload(start)
    log.info(s"Started. upload=${upload.id} multipart=${multipart.getUploadId}")

    val eTags = for(part <- upload.parts.indices)
      yield copyPart(multipart.getUploadId, upload.id, part, fullKey.toString)

    val complete = new CompleteMultipartUploadRequest(
      userUploadBucket, fullKey.toString, multipart.getUploadId, eTags.asJava)

    s3Client.completeMultipartUpload(complete)
    log.info(s"Multipart copy complete. upload=${upload.id} multipart=${multipart.getUploadId}")
  }

  private def completePart(key: UploadPartKey, upload: Upload): Upload = {
    upload.withPart(key.toString) { part =>
      part.copy(uploadedToS3 = true)
    }
  }

  private def allPartsInS3(upload: Upload): Boolean = {
    upload.parts.forall(_.uploadedToS3)
  }

  private def addToStream(key: UploadPartKey): Unit = {
    val bytes = ByteBuffer.wrap(key.toString.getBytes(StandardCharsets.UTF_8))
    kinesisClient.putRecord(youTubeUploadsStreamName, bytes, key.id)
  }

  private def copyPart(multipartId: String, uploadId: String, part: Int, key: String): PartETag = {
    val request = new CopyPartRequest()
      .withUploadId(multipartId)
      .withSourceBucketName(userUploadBucket)
      .withSourceKey(UploadPartKey(userUploadFolder, uploadId, part).toString)
      .withDestinationBucketName(userUploadBucket)
      .withDestinationKey(key.toString)
      .withPartNumber(part + 1)

    log.info(s"Copying upload=$uploadId multipart=$multipartId part=$part")
    val response = s3Client.copyPart(request)

    new PartETag(response.getPartNumber, response.getETag)
  }
}
