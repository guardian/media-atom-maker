package util

import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

/** These functions help to manipulate the subtitle source file given an
  * `upload` object, which represents the state stored in the dynamodb
  * media-atom-maker-pipline-cache table
  */
object SubtitleUtil extends Logging {

  val MimeTypeSrt = "application/x-subrip"
  val MimeTypeVtt = "text/vtt"

  def contentTypeForFilename(filename: String): String = {
    val fileExt = filename.split("\\.").last.toLowerCase
    fileExt match {
      case "srt" => MimeTypeSrt
      case "vtt" => MimeTypeVtt
      case _     => "application/octet-stream"
    }
  }

  def deleteSubtitlesFromUserUploadBucket(
      upload: Upload,
      awsConfig: AWSConfig
  ): Unit = {
    upload.metadata.subtitleSource.foreach { source =>
      // remove subtitle file from s3
      try {
        val request = DeleteObjectRequest.builder().bucket(awsConfig.userUploadBucket).key(source.src).build()
        awsConfig.s3Client.deleteObject(request)
      } catch {
        case e: Throwable =>
          log.warn("error deleting subtitle file", e)
      }
    }
  }
}
