package data

import javax.inject.{Inject, Provider}

import com.gu.atom.publish.{ KinesisAtomReindexer, AtomReindexer }
import util.AWSConfig

class AtomReindexerProvider @Inject() (awsConfig: AWSConfig)
    extends Provider[AtomReindexer] {
  def get() = new KinesisAtomReindexer(
    awsConfig.kinesisReindexStreamName, awsConfig.kinesisClient
  )
}
