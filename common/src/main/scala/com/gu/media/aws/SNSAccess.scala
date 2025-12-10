package com.gu.media.aws

import com.amazonaws.regions.Regions
import software.amazon.awssdk.services.sns.SnsClient
import com.gu.media.Settings

trait SNSAccess { this: Settings with AwsAccess =>

  lazy val capiContentEventsTopicName = getString(
    "aws.sns.content.capi.topicname"
  )

  lazy val snsClient =
    SnsClient
      .builder()
      .region(awsV2Region)
      .credentialsProvider(credentials.instance.awsV2Creds)
      .build()

}
