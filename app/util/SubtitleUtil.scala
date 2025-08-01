package util

import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoSource}
import com.gu.media.upload.model.Upload

/**
 * Self-hosted assets in an atom (such as looping video) have a `sources` property that includes
 * references to the video and subtitle files that have been uploaded to S3.
 *
 * These functions help to extract and manipulate those sources given an `upload` object, which represents the
 * state stored in the dynamodb media-atom-maker-pipline-cache table
 */
object SubtitleUtil extends Logging {

  val MimeTypeSrt = "application/x-subrip"
  val MimeTypeVtt = "text/vtt"

  val SubtitleMimeTypes = Set(MimeTypeSrt, MimeTypeVtt)

  def isSubtitleSource(source: VideoSource): Boolean = SubtitleMimeTypes.contains(source.mimeType)

  def contentTypeForFilename(filename: String): String  = {
    val fileExt = filename.split("\\.").last.toLowerCase
    fileExt match {
      case "srt" => MimeTypeSrt
      case "vtt" => MimeTypeVtt
      case _ => "application/octet-stream"
    }
  }

  def selfHostedSources(upload: Upload): List[VideoSource] = upload.metadata.asset match {
    case Some(asset: SelfHostedAsset) => asset.sources
    case _ => Nil
  }

  def deleteSubtitlesFromUserUploadBucket(sources: List[VideoSource], awsConfig: AWSConfig): Unit = {
    sources.filter(isSubtitleSource).foreach { source =>
      // remove subtitle file from s3
      try {
        val request = new DeleteObjectRequest(awsConfig.userUploadBucket, source.src)
        awsConfig.s3Client.deleteObject(request)
      } catch {
        case e: Throwable =>
          log.warn("error deleting subtitle file", e)
      }
    }
  }

  def updateSourcesOnUpload(upload: Upload, newSources: List[VideoSource]): Upload =
    upload.metadata.asset match {
      case Some(asset: SelfHostedAsset) =>
        val updatedAsset = Some(asset.copy(sources = newSources))
        val updatedMetadata = upload.metadata.copy(asset = updatedAsset)
        upload.copy(metadata = updatedMetadata)
      case _ =>
        upload
    }
}
