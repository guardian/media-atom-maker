package com.gu.media.lambda

import software.amazon.awssdk.services.s3.S3Client
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.gu.media.aws.AwsAccess
import software.amazon.awssdk.services.s3.model.GetObjectRequest

trait LambdaYoutubeCredentials { self: AwsAccess =>
  lazy val youtubeCredentials: GoogleCredential = {
    (sys.env.get("CONFIG_BUCKET"), sys.env.get("CREDENTIALS_KEY")) match {
      case (Some(bucket), Some(key)) =>
        val defaultRegionS3 = S3Client
          .builder()
          .credentialsProvider(credentials.instance.awsV2Creds)
          .region(awsV2Region)
          .build()

        val inputStream = defaultRegionS3.getObject(
          GetObjectRequest.builder().bucket(bucket).key(key).build()
        )
        GoogleCredential.fromStream(inputStream)

      case _ =>
        throw new Exception(
          "Missing environment variables CONFIG_BUCKET/CREDENTIALS_KEY"
        )
    }
  }
}
