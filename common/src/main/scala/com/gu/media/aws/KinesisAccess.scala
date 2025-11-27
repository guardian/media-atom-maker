package com.gu.media.aws

import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.kinesis.model.{
  PutRecordRequest,
  DescribeStreamRequest
}
import software.amazon.awssdk.services.kinesis.KinesisClient
import com.gu.media.Settings
import com.gu.media.logging.Logging
import play.api.libs.json.{Json, Writes}
import software.amazon.awssdk.core.SdkBytes

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

  lazy val crossAccountKinesisClient = KinesisClient
    .builder()
    .credentialsProvider(credentials.crossAccount.awsV2Creds)
    .region(awsV2Region)
    .build()

  lazy val kinesisClient = KinesisClient
    .builder()
    .credentialsProvider(credentials.instance.awsV2Creds)
    .region(awsV2Region)
    .build()

  def sendOnKinesis[T: Writes](
      streamName: String,
      partitionKey: String,
      value: T
  ): Unit = {
    val json = Json.stringify(Json.toJson(value))
    log.info(s"Sending JSON on Kinesis [$streamName]: $json")

    val putRecordRequest = PutRecordRequest
      .builder()
      .streamName(streamName)
      .data(SdkBytes.fromUtf8String(json))
      .partitionKey(partitionKey)
      .build()

    kinesisClient.putRecord(putRecordRequest)
  }

  def testKinesisAccess(streamName: String): Boolean = try {
    val describeStream =
      DescribeStreamRequest.builder().streamName(streamName).build()
    crossAccountKinesisClient.describeStream(describeStream)
    true
  } catch {
    case e: SdkClientException =>
      false
  }
}
