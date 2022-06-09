package com.gu.media.aws

import com.amazonaws.services.s3.AmazonS3ClientBuilder

trait S3Access { this: AwsAccess =>
  lazy val s3Client = AmazonS3ClientBuilder
    .standard()
    .withCredentials(credsProvider)
    .withRegion(region.getName)
    .build()
}
