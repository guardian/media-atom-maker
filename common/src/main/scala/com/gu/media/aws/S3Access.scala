package com.gu.media.aws

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

trait S3Access { this: AwsAccess =>
  lazy val s3Client =
    S3Access.buildClient(credsProvider, region.id())
}

object S3Access {
  def buildClient(
      credsProvider: AwsCredentialsProvider,
      region: String
  ) =
    S3Client
      .builder()
      .credentialsProvider(credsProvider)
      .region(Region.of(region))
      .build()
}
