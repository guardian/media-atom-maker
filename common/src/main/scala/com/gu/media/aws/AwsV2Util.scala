package com.gu.media.aws

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.awscore.client.builder.{
  AwsClientBuilder,
  AwsSyncClientBuilder
}
import software.amazon.awssdk.http.apache5.Apache5HttpClient
import software.amazon.awssdk.regions.Region

object AwsV2Util {
  def buildSync[T, B <: AwsClientBuilder[B, T] with AwsSyncClientBuilder[B, T]](
      builder: B,
      creds: AwsCredentialsProvider,
      region: Region
  ): T = builder
    .httpClientBuilder(Apache5HttpClient.builder())
    .credentialsProvider(creds)
    .region(region)
    .build()
}
