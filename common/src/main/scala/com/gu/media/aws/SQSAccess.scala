package com.gu.media.aws

import software.amazon.awssdk.services.sqs.SqsClient
import com.gu.media.Settings

trait SQSAccess { this: Settings with AwsAccess =>

  lazy val plutoQueueUrl: String = getMandatoryString("aws.sqs.plutoQueueUrl")
  lazy val iconikQueueUrl: String = getMandatoryString("aws.sqs.iconikQueueUrl")

  lazy val sqsClient =
    SqsClient
      .builder()
      .credentialsProvider(credentials.instance.awsV2Creds)
      .region(awsV2Region)
      .build()
}
