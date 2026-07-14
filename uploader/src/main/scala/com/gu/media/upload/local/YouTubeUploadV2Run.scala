package com.gu.media.upload.local
import com.gu.media.Settings
import com.gu.media.aws.{AwsAccess, S3Access}
import com.gu.media.lambda.{LambdaBase, LambdaYoutubeCredentials}
import com.gu.media.logging.Logging
import com.gu.media.model.PlutoSyncMetadataMessage
import com.gu.media.upload.model.{
  Upload,
  UploadMetadata,
  UploadPart,
  UploadProgress
}
import com.gu.media.upload.youtubeuploadv2.YouTubeUploadV2
import com.gu.media.youtube.{YouTubeAccess, YouTubeUpload}

object YouTubeUploadV2Run
    extends App
    with LambdaBase
    with LambdaYoutubeCredentials
    with Logging
    with AwsAccess
    with S3Access
    with YouTubeAccess
    with Settings {

  val bucket = "media-atom-maker-upload-code"
  val key = "uploads/01290fad-d87d-452f-b080-2d53fb4575a8-1/complete"
  val size = 1000L

  val exampleUpload = Upload(
    s"ace3fcf6-1378-41db-9d21-f3fc07072ab2",
    List(UploadPart("key", 0L, size)),
    UploadMetadata(
      "jo.blogs@guardian.co.uk",
      bucket,
      "region",
      "title",
      PlutoSyncMetadataMessage("", None, key, "", "", "", None),
      iconikData = None,
      null,
      originalFilename = Some(""),
      subtitleSource = None,
      subtitleVersion = None,
      startTimestamp = 1L,
      selfHost = true
    ),
    UploadProgress(1, 0, true, true, 0)
  )

  YouTubeUploadV2.run(exampleUpload)

}
