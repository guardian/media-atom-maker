package com.gu.media.upload

import com.gu.media.Settings
import com.gu.media.aws.{AwsAccess, S3Access}
import com.gu.media.lambda.{
  LambdaBase,
  LambdaWithParams,
  LambdaYoutubeCredentials
}
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload
import com.gu.media.upload.youtubeuploadv2.YouTubeUploadV2
import com.gu.media.youtube.YouTubeAccess

class YouTubeUploadV2
    extends LambdaWithParams[Upload, Upload]
    with LambdaBase
    with LambdaYoutubeCredentials
    with Logging
    with AwsAccess
    with S3Access
    with YouTubeAccess
    with Settings {

  override def handle(input: Upload): Upload = {
    YouTubeUploadV2.run(input)
    input
  }
}
