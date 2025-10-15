package com.gu.media.aws

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

trait S3Access { this: AwsAccess =>
  lazy val s3Client: AmazonS3 =
    S3Access.buildClient(credsProvider, region.getName)
}

object S3Access {
  def buildClient(
      credsProvider: AWSCredentialsProvider,
      region: String
  ): AmazonS3 =
    AmazonS3ClientBuilder
      .standard()
      .withCredentials(credsProvider)
      .withRegion(region)
      .build()
}
