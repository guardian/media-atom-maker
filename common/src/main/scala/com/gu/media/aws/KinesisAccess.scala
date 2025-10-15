package com.gu.media.aws

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.amazonaws.AmazonClientException
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder
import com.gu.media.Settings
import com.gu.media.logging.Logging
import play.api.libs.json.{Json, Writes}

trait KinesisAccess { this: Settings with AwsAccess with Logging =>
  val liveKinesisStreamName: String = getMandatoryString(
    "aws.kinesis.liveStreamName"
  )
  val previewKinesisStreamName: String = getMandatoryString(
    "aws.kinesis.previewStreamName"
  )

  val previewKinesisReindexStreamName: String = getMandatoryString(
    "aws.kinesis.previewReindexStreamName"
  )
  val publishedKinesisReindexStreamName: String = getMandatoryString(
    "aws.kinesis.publishedReindexStreamName"
  )

  val plutoIntegrationOutgoingStream: String = getMandatoryString(
    "aws.kinesis.uploadsStreamName"
  )

  val syncWithPluto: Boolean = getBoolean("pluto.sync").getOrElse(false)

  lazy val crossAccountKinesisClient = AmazonKinesisClientBuilder
    .standard()
    .withCredentials(credentials.crossAccount.awsV1Creds)
    .withRegion(region.getName)
    .build()

  lazy val kinesisClient = AmazonKinesisClientBuilder
    .standard()
    .withCredentials(credentials.instance.awsV1Creds)
    .withRegion(region.getName)
    .build()

  def sendOnKinesis[T: Writes](
      streamName: String,
      partitionKey: String,
      value: T
  ): Unit = {
    val json = Json.stringify(Json.toJson(value))
    log.info(s"Sending JSON on Kinesis [$streamName]: $json")

    val bytes = ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8))

    kinesisClient.putRecord(streamName, bytes, partitionKey)
  }

  def testKinesisAccess(streamName: String): Boolean = try {
    crossAccountKinesisClient.describeStream(streamName)
    true
  } catch {
    case e: AmazonClientException =>
      false
  }
}
