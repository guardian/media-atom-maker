package com.gu.atom.publish

import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.gu.contentatom.thrift.ContentAtomEvent
import scala.concurrent.{ Future, ExecutionContext }

class KinesisAtomReindexer(
  streamName: String,
  kinesis: AmazonKinesisClient)
    extends AtomReindexer
    with ThriftSerializer[ContentAtomEvent] {

  def makeParititionKey(event: ContentAtomEvent): String = event.atom.atomType.name

  def startReindexJob(atomEventsToReindex: Iterator[ContentAtomEvent], expectedSize: Int)(implicit ec: ExecutionContext) =
    new AtomReindexJob(atomEventsToReindex, expectedSize) {
      def execute = Future {
        atomEventsToReindex foreach { atomEvent =>
          kinesis.putRecord(streamName, serializeEvent(atomEvent), makeParititionKey(atomEvent))
          _completedCount += 1
        }
        _isComplete = true
        _completedCount
      }
    }
}
 
