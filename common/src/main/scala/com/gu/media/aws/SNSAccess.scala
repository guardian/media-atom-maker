package com.gu.media.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.gu.media.Settings

trait SNSAccess { this: Settings with AwsAccess =>

  lazy val capiContentEventsTopicName = getString(
    "aws.sns.content.capi.topicname"
  )

  lazy val snsClient =
    AmazonSNSClientBuilder
      .standard()
      .withRegion(Regions.fromName(region.getName))
      .withCredentials(credentials.instance.awsV1Creds)
      .build()
}
