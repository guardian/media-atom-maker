package data

import com.gu.contentatom.thrift.ContentAtomEvent

import com.amazonaws.services.kinesis.AmazonKinesisClient
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import scala.util.Try

class KinesisAtomPublisher @Inject() (config: Configuration)
    extends AtomPublisher
    with ThriftSerializer[ContentAtomEvent] {

  lazy val kinesis: AmazonKinesisClient = new AmazonKinesisClient()
  val streamName = config.getString("kinesis.streamName").get

  def makeParititionKey(event: ContentAtomEvent): String = event.atom.atomType.name

  def publishAtomEvent(event: ContentAtomEvent) = Try {
    val data = serializeEvent(event)
    kinesis.putRecord(streamName, data, makeParititionKey(event))
  }
}
