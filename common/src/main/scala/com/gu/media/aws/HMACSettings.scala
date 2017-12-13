package com.gu.media.aws

import java.net.URI

import com.gu.media.Settings

trait HMACSettings { this: Settings with AwsAccess =>
  lazy val sharedSecret: String = getMandatoryString("secret")
  lazy val hostname: String = getMandatoryString("host")

  def buildUri(path: String): URI = {
    URI.create(s"https://$hostname/$path")
  }
}
