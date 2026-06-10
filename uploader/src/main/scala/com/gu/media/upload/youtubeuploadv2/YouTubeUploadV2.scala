package com.gu.media.upload.youtubeuploadv2

import com.gu.media.Settings
import com.gu.media.aws.{AwsAccess, S3Access}
import com.gu.media.lambda.{LambdaBase, LambdaYoutubeCredentials}
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload
import com.gu.media.youtube.{YouTubeAccess, YouTubeUploader}
import com.gu.media.youtube.YouTubeUpload.{log, trainingChannels}

import java.util.UUID

object YouTubeUploadV2
    extends LambdaBase
    with LambdaYoutubeCredentials
    with Logging
    with AwsAccess
    with S3Access
    with YouTubeAccess
    with Settings {

  def run(upload: Upload) = {
    log.info("running upload to youtube v2")
    val uploader = new YouTubeUploader(this, this.s3Client)
    val size = upload.parts.last.end
    val bucket = upload.metadata.bucket
    val s3Key = upload.metadata.pluto.s3Key

    val uploadUri = uploader.startUpload(
      "test",
      trainingChannels.head,
      UUID.randomUUID().toString,
      size
    )
    log.info(uploadUri)
    //    val response = uploader.uploadFull(
    //      bucket,
    //      s3Key,
    //      uploadUri,
    //      size
    //    )
    //    response
  }

}
