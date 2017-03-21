package com.gu.media.upload.actions

import com.amazonaws.services.s3.model.{CompleteMultipartUploadRequest, CopyPartRequest, InitiateMultipartUploadRequest, PartETag}
import com.gu.media.logging.Logging
import com.gu.media.upload._
import com.gu.media.youtube.YouTubeUploader

import scala.util.control.NonFatal
import scala.collection.JavaConverters._

abstract class UploadActionHandler(store: UploadsDataStore, s3: S3UploadAccess, youTube: YouTubeUploader) extends Logging {
  // Returns the new version number
  def addAsset(atomId: String, videoId: String): Long

  def handle(action: UploadAction): Unit = action match {
    case UploadPartToYouTube(uploadId, key) =>
      uploadToYouTube(uploadId, key)

    case CopyParts(uploadId, destination) =>
      createCompleteObject(uploadId, destination)

    case DeleteParts(uploadId) =>
      deleteParts(uploadId)
  }

  private def uploadToYouTube(uploadId: String, partKey: String): Unit = {
    getUploadAndPart(uploadId, partKey) match {
      case Some((upload, part)) =>
        val total = upload.parts.last.end
        val uploadUri = getYTUploadUrl(upload)

        log.info(s"Uploading ${part.key} [${part.start} - ${part.end}]")

        youTube.uploadPart(uploadUri, part.key, part.start, part.end, total, (_: Long) => {}).foreach { videoId =>
          log.info(s"Successful upload ${upload.id}. YouTube ID: $videoId")

          addAsset(upload.metadata.atomId, videoId)
          store.delete(upload.id)
        }

      case _ =>
        log.error(s"Unknown upload id $uploadId or part $partKey")
    }
  }

  private def getYTUploadUrl(upload: Upload): String = upload.youTube.upload.getOrElse {
    log.info(s"Starting YouTube upload for ${upload.id} [${upload.metadata.title} - ${upload.youTube.channel}]")

    val url = youTube.startUpload(upload.metadata.title, upload.youTube.channel, upload.id, upload.parts.last.end)
    val updated = upload.copy(youTube = upload.youTube.copy(upload = Some(url)))

    store.put(updated)
    url
  }

  // Videos are uploaded as a series of smaller parts. In order to simplify Pluto ingestion and allow us
  // to transcode if required, we use S3 multipart copy to create a new object consisting of all the parts
  private def createCompleteObject(uploadId: String, destination: String): Unit = {
    store.get(uploadId) match {
      case Some(upload) =>
        val start = new InitiateMultipartUploadRequest(s3.userUploadBucket, destination)
        log.info(s"Starting multipart copy for upload ${upload.id}")

        val multipart = s3.s3Client.initiateMultipartUpload(start)
        log.info(s"Started. upload=${upload.id} multipart=${multipart.getUploadId}")

        val eTags = for (part <- upload.parts.indices)
          yield copyPart(multipart.getUploadId, upload.id, part, destination)

        val complete = new CompleteMultipartUploadRequest(
          s3.userUploadBucket, destination, multipart.getUploadId, eTags.asJava)

        s3.s3Client.completeMultipartUpload(complete)

        log.info(s"Multipart copy complete. upload=${upload.id} multipart=${multipart.getUploadId}")

      case None =>
        log.error(s"Unknown upload id $uploadId")
    }
  }

  private def copyPart(multipartId: String, uploadId: String, part: Int, key: String): PartETag = {
    val request = new CopyPartRequest()
      .withUploadId(multipartId)
      .withSourceBucketName(s3.userUploadBucket)
      .withSourceKey(UploadPartKey(s3.userUploadFolder, uploadId, part).toString)
      .withDestinationBucketName(s3.userUploadBucket)
      .withDestinationKey(key.toString)
      .withPartNumber(part + 1)

    log.info(s"Copying upload=$uploadId multipart=$multipartId part=$part")
    val response = s3.s3Client.copyPart(request)

    new PartETag(response.getPartNumber, response.getETag)
  }

  private def deleteParts(uploadId: String): Unit = {
    store.get(uploadId) match {
      case Some(upload) =>
        // The complete key will be deleted once it has been ingested by Pluto
        upload.parts.foreach { part =>
          try {
            log.info(s"Deleting part $part")
            s3.s3Client.deleteObject(s3.userUploadBucket, part.key)
          } catch {
            case NonFatal(err) =>
              // if we can't delete it, no problem. the bucket policy will remove it in time
              log.warn(s"Unable to delete part $part: $err")
          }
        }

      case None =>
        log.error(s"Unknown upload id $uploadId")
    }
  }

  private def getUploadAndPart(uploadId: String, partKey: String): Option[(Upload, UploadPart)] = {
    for {
      upload <- store.get(uploadId)
      part <- upload.parts.find(_.key == partKey)
    } yield (upload, part)
  }
}
