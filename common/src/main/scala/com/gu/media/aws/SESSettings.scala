package com.gu.media.aws

import com.gu.media.Settings
import software.amazon.awssdk.services.ses.SesClient

trait SESSettings { this: Settings with AwsAccess =>

  lazy val sesClient = SesClient
    .builder()
    .credentialsProvider(credentials.instance.awsV2Creds)
    .region(awsV2Region)
    .build()

  val fromEmailAddress = getMandatoryString("aws.ses.fromEmailAddress")

  val replyToAddresses: Seq[String] =
    getMandatoryString("aws.ses.replyToAddresses").split(",").toSeq

  val integrationTestUser: String = getMandatoryString("integration.test.user")

  val expiryNotificationsAddress = getMandatoryString(
    "aws.ses.expiryNotificationsAddress"
  )

  val host: String = getMandatoryString("host")
}
