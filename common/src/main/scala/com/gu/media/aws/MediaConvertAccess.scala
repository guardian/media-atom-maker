package com.gu.media.aws

import software.amazon.awssdk.services.mediaconvert.MediaConvertClient
import com.gu.media.Settings

trait MediaConvertAccess { this: Settings with AwsAccess =>
  // lazy val transcodePipelineId = getMandatoryString("aws.transcoder.pipelineId") // perhaps replace this concept with job tags?

  lazy val mediaConvertRole = sys.env.getOrElse(
    "MEDIA_CONVERT_ROLE",
    getMandatoryString("aws.mediaconvert.role")
  )

  lazy val destinationBucket = getMandatoryString(
    "aws.mediaconvert.destinationBucket"
  )

  lazy val mediaConvertClient = MediaConvertClient
    .builder()
    .credentialsProvider(credentials.instance.awsV2Creds)
    .region(awsV2Region)
    .build()
}
