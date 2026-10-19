package com.gu.media.aws

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  StaticCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class DynamoDbAsyncClientTest extends AnyFlatSpec with Matchers {

  "DynamoDbAsyncClient" should "build using the existing runtime HTTP provider" in {
    val client = DynamoDbAsyncClient
      .builder()
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create("test-access-key", "test-secret-key")
        )
      )
      .region(Region.EU_WEST_1)
      .build()

    client.close()
  }
}
