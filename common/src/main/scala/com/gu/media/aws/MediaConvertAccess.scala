package com.gu.media.aws

import com.amazonaws.services.mediaconvert.AWSMediaConvertClientBuilder
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

  lazy val mediaConvertClient = AWSMediaConvertClientBuilder
    .standard()
    .withCredentials(credsProvider)
    .withRegion(region.getName)
    .build()
}
