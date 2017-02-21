package data

import javax.inject.{Inject, Provider}

import com.gu.atom.publish.{PublishedKinesisAtomReindexer, PublishedAtomReindexer, PreviewKinesisAtomReindexer, PreviewAtomReindexer}
import util.AWSConfig

class PreviewAtomReindexerProvider(awsConfig: AWSConfig)
    extends Provider[PreviewAtomReindexer] {
  def get() = new PreviewKinesisAtomReindexer(
    awsConfig.previewKinesisReindexStreamName, awsConfig.kinesisClient
  )
}

class PublishedAtomReindexerProvider(awsConfig: AWSConfig)
  extends Provider[PublishedAtomReindexer] {
  def get() = new PublishedKinesisAtomReindexer(
    awsConfig.publishedKinesisReindexStreamName, awsConfig.kinesisClient
  )
}
