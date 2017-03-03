package com.gu.media.aws

import com.amazonaws.services.sqs.AmazonSQSClient

import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.gu.media.Settings

trait SQSAccess { this: Settings with AwsAccess =>

  lazy val uploadedQueueUrl = getMandatoryString("aws.sqs.uploadedQueueUrl")
  lazy val plutoTopicArn = getMandatoryString("aws.sqs.plutoTopicArn")
  val snsClient = AmazonSNSClientBuilder.standard().withRegion("eu-west-1").withCredentials(credsProvider).build()

  lazy val sqsClient =
    new AmazonSQSClient(credsProvider)
}
