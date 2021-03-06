package com.gu.media.aws

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.gu.media.Settings

trait SESSettings { this: Settings with AwsAccess =>

  lazy val sesClient = region.createClient(classOf[AmazonSimpleEmailServiceClient],
    credsProvider, null)

  val fromEmailAddress = getMandatoryString("aws.ses.fromEmailAddress")

  val replyToAddresses: Seq[String] = getMandatoryString("aws.ses.replyToAddresses").split(",").toSeq

  val integrationTestUser: String = getMandatoryString("integration.test.user")

  val host: String = getMandatoryString("host")
}
