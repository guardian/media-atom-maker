package com.gu.media.aws

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.amazonaws.AmazonClientException
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.gu.media.Settings
import play.api.libs.json.{Json, Writes}

trait KinesisAccess { this: Settings with AwsAccess =>
  val liveKinesisStreamName: String = getMandatoryString("aws.kinesis.liveStreamName")
  val previewKinesisStreamName: String = getMandatoryString("aws.kinesis.previewStreamName")

  val previewKinesisReindexStreamName: String = getMandatoryString("aws.kinesis.previewReindexStreamName")
  val publishedKinesisReindexStreamName: String = getMandatoryString("aws.kinesis.publishedReindexStreamName")

  val uploadsStreamName: String = getMandatoryString("aws.kinesis.uploadsStreamName")
  val uploadActionsStreamName: String = getMandatoryString("aws.kinesis.uploadActionsStreamName")

  lazy val crossAccountKinesisClient = region.createClient(classOf[AmazonKinesisClient], credentials.crossAccount, null)
  lazy val kinesisClient = region.createClient(classOf[AmazonKinesisClient], credentials.instance, null)

  def sendOnKinesis[T: Writes](streamName: String, partitionKey: String, value: T): Unit = {
    val json = Json.stringify(Json.toJson(value))
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
