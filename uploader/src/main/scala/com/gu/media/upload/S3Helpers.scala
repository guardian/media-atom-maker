package com.gu.media.upload

import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import com.gu.media.aws.AwsAccess

import java.time.Duration

trait S3Helpers { this: AwsAccess =>

  lazy val s3Presigner: S3Presigner =
    S3Presigner
      .builder()
      .credentialsProvider(credentials.instance.awsV2Creds)
      .region(awsV2Region)
      .build()

  /** Generate a presigned GET URL so videos can be read directly over HTTPS.
    *
    * @see
    *   https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3-presign.html
    */
  def generatePresignedDownloadUrl(
      bucket: String,
      key: String,
      /* 1 hour is chosen here as url needs to be available longer that the video takes to stream
      * 1 hour should be longer than most videos
      * */
      expiration: Duration = Duration.ofHours(1)
  ): String = {
    val getObjectRequest =
      GetObjectRequest
        .builder()
        .bucket(bucket)
        .key(key)
        .build()

    val presignRequest =
      GetObjectPresignRequest
        .builder()
        .signatureDuration(expiration)
        .getObjectRequest(getObjectRequest)
        .build()

    s3Presigner.presignGetObject(presignRequest).url().toExternalForm
  }
}
