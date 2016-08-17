package data

import javax.inject.{Inject, Provider}

import com.gu.atom.publish.{PublishedKinesisAtomReindexer, PublishedAtomReindexer, PreviewKinesisAtomReindexer, PreviewAtomReindexer}
import util.AWSConfig

class PreviewAtomReindexerProvider @Inject() (awsConfig: AWSConfig)
    extends Provider[PreviewAtomReindexer] {
  def get() = new PreviewKinesisAtomReindexer(
    awsConfig.kinesisReindexStreamName, awsConfig.kinesisClient
  )
}

class PublishedAtomReindexerProvider @Inject() (awsConfig: AWSConfig)
  extends Provider[PublishedAtomReindexer] {
  def get() = new PublishedKinesisAtomReindexer(
    awsConfig.kinesisReindexStreamName, awsConfig.kinesisClient
  )
}
