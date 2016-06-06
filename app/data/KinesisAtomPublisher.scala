package data

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.gu.contentatom.thrift.ContentAtomEvent

import com.amazonaws.services.kinesis.AmazonKinesisClient
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import scala.util.Try

class KinesisAtomPublisher (val streamName: String, val kinesis: AmazonKinesisClient)
    extends AtomPublisher
    with ThriftSerializer[ContentAtomEvent]
    with com.typesafe.scalalogging.LazyLogging {

  logger.info(s"KinsisAtomPublisher started with streamName $streamName")

  private def awsClient = new AmazonKinesisClient

  @Inject() def this(config: Configuration) = this(
    config.getString("aws.kinesis.streamName").get,
    KinesisAtomPublisher.clientFromConfig(config)
  )

  def makeParititionKey(event: ContentAtomEvent): String = event.atom.atomType.name

  def publishAtomEvent(event: ContentAtomEvent): Try[Unit] = Try {
      val data = serializeEvent(event)
      kinesis.putRecord(streamName, data, makeParititionKey(event))
    }
}

object KinesisAtomPublisher {
  def clientFromConfig(config: Configuration): AmazonKinesisClient = {
    val cred = config.getString("aws.profile").map(new ProfileCredentialsProvider(_)).get
    new AmazonKinesisClient(cred)
  }
}
