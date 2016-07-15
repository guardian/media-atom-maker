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

  def startReindexJob(atomEventsToReindex: Iterator[ContentAtomEvent], expectedSize: Int) =
    new AtomReindexJob(atomEventsToReindex, expectedSize) {
      def execute(implicit ec: ExecutionContext) = Future {
        atomEventsToReindex foreach { atomEvent =>
          println(s"PMR putting record (${completedCount})")
          kinesis.putRecord(streamName, serializeEvent(atomEvent), makeParititionKey(atomEvent))
          _completedCount += 1
        }
        println(s"PMR complete (${completedCount})")
        _isComplete = true
        _completedCount
      }
    }
}
 
