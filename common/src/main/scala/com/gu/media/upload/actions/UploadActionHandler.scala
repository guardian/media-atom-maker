package com.gu.media.upload.actions

import com.amazonaws.services.s3.model._
import com.gu.media.PlutoDataStore
import com.gu.media.logging.Logging
import com.gu.media.ses.Mailer
import com.gu.media.upload._
import com.gu.media.upload.model.{PlutoSyncMetadata, Upload, UploadPart}
import com.gu.media.youtube.YouTubeUploader

import scala.util.control.NonFatal
import scala.collection.JavaConverters._

abstract class UploadActionHandler(store: UploadsDataStore, plutoStore: PlutoDataStore, uploaderAccess: UploaderAccess,
                                   youTube: YouTubeUploader, mailer: Mailer)
  extends Logging {

  private val s3 = uploaderAccess.s3Client
  private val bucket = uploaderAccess.userUploadBucket

  // Returns the new version number
  def addAsset(atomId: String, videoId: String): Long

  def handle(action: UploadAction): Unit = action match {
    case UploadPartToYouTube(upload, part, uploadUri) =>
      val updated = uploadToYouTube(upload, part, uploadUri)
      val withProgress = updated.copy(progress = upload.progress.copy(uploadedToYouTube = part.end))

      store.report(withProgress)

    case CopyParts(upload, destination) =>
      createCompleteObject(upload, destination)
      sendToPluto(upload)

    case DeleteParts(upload) =>
      deleteParts(upload)
      store.delete(upload.id)
  }

  private def uploadToYouTube(upload: Upload, part: UploadPart, uploadUri: String): Upload = {
    log.info(s"Uploading ${part.key} [${part.start} - ${part.end}]")

    val UploadPart(key, start, end) = part
    val total = upload.parts.last.end

    if(objectExists(key.toString)) {
      val input = s3.getObject(bucket, key.toString).getObjectContent

      youTube.uploadPart(uploadUri, input, start, end, total) match {
        case Some(videoId) =>
          // last part. add asset
          val version = addAsset(upload.metadata.pluto.atomId, videoId)
          val plutoData = upload.metadata.pluto.copy(assetVersion = version)

          upload.copy(metadata = upload.metadata.copy(pluto = plutoData))

        case None =>
          upload
      }
    } else {
      log.error(s"Unable to upload ${part.key} since it has been deleted from S3")
      upload
    }
  }

  // Videos are uploaded as a series of smaller parts. In order to simplify Pluto ingestion and allow us
  // to transcode if required, we use S3 multipart copy to create a new object consisting of all the parts
  private def createCompleteObject(upload: Upload, destination: String): Unit = {
    val start = new InitiateMultipartUploadRequest(bucket, destination)
    log.info(s"Starting multipart copy for upload ${upload.id}")

    val multipart = s3.initiateMultipartUpload(start)

    val eTags = for (part <- upload.parts.indices)
      yield copyPart(multipart.getUploadId, upload.id, part, destination)

    val complete = new CompleteMultipartUploadRequest(
      bucket, destination, multipart.getUploadId, eTags.asJava)

    s3.completeMultipartUpload(complete)

    log.info(s"Multipart copy complete. upload=${upload.id} multipart=${multipart.getUploadId}")
  }

  private def sendToPluto(upload: Upload): Unit = {

    val plutoData: PlutoSyncMetadata = upload.metadata.pluto

    plutoData.projectId match {
      case _ if plutoData.assetVersion == -1 =>
        // TODO: work out what to do here? probably need to manually add the asset

      case Some(project) =>
        uploaderAccess.sendOnKinesis(uploaderAccess.uploadsStreamName, plutoData.s3Key, plutoData)

      case None =>
        val metadata = upload.metadata
        mailer.sendPlutoIdMissingEmail(metadata.title, metadata.user, uploaderAccess.fromEmailAddress,
          uploaderAccess.replyToAddresses)
        plutoStore.put(plutoData)
    }
  }

  private def copyPart(multipartId: String, uploadId: String, part: Int, key: String): PartETag = {
    val request = new CopyPartRequest()
      .withUploadId(multipartId)
      .withSourceBucketName(bucket)
      .withSourceKey(UploadPartKey(uploaderAccess.userUploadFolder, uploadId, part).toString)
      .withDestinationBucketName(bucket)
      .withDestinationKey(key.toString)
      .withPartNumber(part + 1)

    log.info(s"Copying upload=$uploadId multipart=$multipartId part=$part")
    val response = s3.copyPart(request)

    new PartETag(response.getPartNumber, response.getETag)
  }

  private def deleteParts(upload: Upload): Unit = {
    // The complete key will be deleted once it has been ingested by Pluto
    upload.parts.foreach { part =>
      try {
        log.info(s"Deleting part $part")
        s3.deleteObject(bucket, part.key)
      } catch {
        case NonFatal(err) =>
          // if we can't delete it, no problem. the bucket policy will remove it in time
          log.warn(s"Unable to delete part $part: $err")
      }
    }
  }

  private def objectExists(key: String) = try {
    s3.doesObjectExist(bucket, key)
  } catch {
    case e: AmazonS3Exception =>
      log.error(s"Error checking $key", e)
      false
  }
}
