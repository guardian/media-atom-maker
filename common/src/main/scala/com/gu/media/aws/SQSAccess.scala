package com.gu.media.aws

import com.amazonaws.services.sqs.AmazonSQSClient

import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.gu.media.Settings

trait SQSAccess { this: Settings with AwsAccess =>

  lazy val plutoQueueUrl = getMandatoryString("aws.sqs.plutoQueueUrl")

  lazy val sqsClient =
    new AmazonSQSClient(credsProvider)
}
