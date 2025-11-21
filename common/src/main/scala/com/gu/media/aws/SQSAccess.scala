package com.gu.media.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.{AmazonSQS, AmazonSQSClientBuilder}
import com.gu.media.Settings

trait SQSAccess { this: Settings with AwsAccess =>

  lazy val plutoQueueUrl: String = getMandatoryString("aws.sqs.plutoQueueUrl")

  lazy val iconikQueueUrl: String = getMandatoryString("aws.sqs.iconikQueueUrl")

  lazy val sqsClient: AmazonSQS =
    AmazonSQSClientBuilder
      .standard()
      .withCredentials(credsProvider)
      .withRegion(Regions.EU_WEST_1)
      .build()
}
