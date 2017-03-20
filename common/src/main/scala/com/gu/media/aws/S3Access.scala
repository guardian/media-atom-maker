package com.gu.media.aws

import com.amazonaws.services.s3.AmazonS3Client

trait S3Access { this: AwsAccess =>
  lazy val s3Client: AmazonS3Client = region.createClient(classOf[AmazonS3Client], credsProvider, null)
}
