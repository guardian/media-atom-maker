package com.gu.media.aws

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.amazonaws.AmazonClientException
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.gu.media.Settings
import com.gu.media.logging.Logging
import play.api.libs.json.{Json, Writes}

trait KinesisAccess { this: Settings with AwsAccess with Logging =>
  val liveKinesisStreamName: String = getMandatoryString("aws.kinesis.liveStreamName")
  val previewKinesisStreamName: String = getMandatoryString("aws.kinesis.previewStreamName")

  val previewKinesisReindexStreamName: String = getMandatoryString("aws.kinesis.previewReindexStreamName")
  val publishedKinesisReindexStreamName: String = getMandatoryString("aws.kinesis.publishedReindexStreamName")

  val uploadsStreamName: String = getMandatoryString("aws.kinesis.uploadsStreamName")

  lazy val crossAccountKinesisClient = region.createClient(classOf[AmazonKinesisClient], credentials.crossAccount, null)
  lazy val kinesisClient = region.createClient(classOf[AmazonKinesisClient], credentials.instance, null)

  def sendOnKinesis[T: Writes](streamName: String, partitionKey: String, value: T): Unit = {
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
