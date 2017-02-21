package com.gu.media.aws

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.kinesis.AmazonKinesisClient

trait KinesisAccess { this: AwsAccess with CrossAccountAccess =>
  val liveKinesisStreamName: String = getMandatoryString("aws.kinesis.liveStreamName")
  val previewKinesisStreamName: String = getMandatoryString("aws.kinesis.previewStreamName")

  val previewKinesisReindexStreamName: String = getMandatoryString("aws.kinesis.previewReindexStreamName")
  val publishedKinesisReindexStreamName: String = getMandatoryString("aws.kinesis.publishedReindexStreamName")

  val readFromComposerAccount: Boolean = getBoolean("readFromComposer").getOrElse(false)
  val atomEventsProvider: AWSCredentialsProvider = getCrossAccountCredentials("media-atom-maker-atom-events")

  lazy val kinesisClient = if (stage != "DEV" || readFromComposerAccount) {
    region.createClient(classOf[AmazonKinesisClient], atomEventsProvider, null)
  } else {
    region.createClient(classOf[AmazonKinesisClient], credsProvider, null)
  }
}
