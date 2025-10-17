package com.gu.media.lambda

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.gu.media.aws.AwsAccess

trait LambdaYoutubeCredentials { self: AwsAccess =>
  lazy val youtubeCredentials: GoogleCredential = {
    (sys.env.get("CONFIG_BUCKET"), sys.env.get("CREDENTIALS_KEY")) match {
      case (Some(bucket), Some(key)) =>
        val defaultRegionS3 = AmazonS3ClientBuilder
          .standard()
          .withCredentials(credentials.instance.awsV1Creds)
          .withRegion(region.getName)
          .build()

        val obj = defaultRegionS3.getObject(bucket, key)
        val rawCredentials = obj.getObjectContent
        GoogleCredential.fromStream(rawCredentials)

      case _ =>
        throw new Exception(
          "Missing environment variables CONFIG_BUCKET/CREDENTIALS_KEY"
        )
    }
  }
}
