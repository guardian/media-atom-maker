package data

import com.gu.contentatom.thrift.ContentAtomEvent

import com.amazonaws.services.kinesis.AmazonKinesisClient
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import scala.util.Try

class KinesisAtomPublisher (val streamName: String, val kinesis: AmazonKinesisClient)
    extends AtomPublisher
    with ThriftSerializer[ContentAtomEvent] {

  @Inject() def this(config: Configuration) = this(
    config.getString("aws.kinesis.streamName").get,
    new AmazonKinesisClient()
  )

  def makeParititionKey(event: ContentAtomEvent): String = event.atom.atomType.name

  def publishAtomEvent(event: ContentAtomEvent): Try[Unit] = Try {
      val data = serializeEvent(event)
      kinesis.putRecord(streamName, data, makeParititionKey(event))
    }
}
