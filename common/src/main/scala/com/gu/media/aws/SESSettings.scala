package com.gu.media.aws

import com.gu.media.Settings

trait SESSettings { this: Settings =>
  val fromEmailAddress = getMandatoryString("aws.ses.fromEmailAddress")

  val replyToAddresses = getMandatoryString("aws.ses.replyToAddresses")

}
