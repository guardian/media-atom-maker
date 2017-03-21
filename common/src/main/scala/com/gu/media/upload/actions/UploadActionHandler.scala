package com.gu.media.upload.actions

import com.gu.media.logging.Logging
import com.gu.media.upload.{Upload, UploadPart, UploadsDataStore}
import com.gu.media.youtube.YouTubeUploader

import scala.util.control.NonFatal

abstract class UploadActionHandler(store: UploadsDataStore, s3: S3UploadAccess, youTube: YouTubeUploader) extends Logging {
  // Returns the new version number
  def addAsset(atomId: String, videoId: String): Long

  def handle(action: UploadAction): Unit = action match {
    case UploadPartToYouTube(uploadId, key) =>
      uploadToYouTube(uploadId, key)

    case DeleteParts(uploadId, partsToDelete) =>
      deleteParts(partsToDelete)
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

  private def deleteParts(partsToDelete: List[String]): Unit = {
    // The complete key will be deleted once it has been ingested by Pluto
    partsToDelete.foreach { part =>
      try {
        log.info(s"Deleting part $part")
        s3.s3Client.deleteObject(s3.userUploadBucket, part)
      } catch {
        case NonFatal(err) =>
          // if we can't delete it, no problem. the bucket policy will remove it in time
          log.warn(s"Unable to delete part $part: $err")
      }
    }
  }

  private def getUploadAndPart(uploadId: String, partKey: String): Option[(Upload, UploadPart)] = {
    for {
      upload <- store.get(uploadId)
      part <- upload.parts.find(_.key == partKey)
    } yield (upload, part)
  }
}
