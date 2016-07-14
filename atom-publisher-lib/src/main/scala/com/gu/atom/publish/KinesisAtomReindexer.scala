package com.gu.atom.publish

import akka.actor.{ Actor, Props, ActorSystem }
import akka.util.Timeout
import com.gu.contentatom.thrift.{ Atom, ContentAtomEvent }
import com.amazonaws.services.kinesis.AmazonKinesisClient
import akka.pattern.ask
import scala.concurrent.{ Future, ExecutionContext }

import scala.concurrent.duration._

// class KinesisAtomActor(streamName: String,
//                        kinesis: AmazonKinesisClient,
//                        executionContext: ExecutionContext)
//     extends Actor {

//   implicit val ec = executionContext

//   def processJob(atoms: Iterator[Atom]) = Future {
//     atoms.foldLeft(0) { (count, atom) =>
//       println(atom)
//       count + 1
//     }
//   }

//   val inProgress: Receive = { case _ => InProgress }

//   def waiting(lastStatus: Option[Status]): Receive = {
//     case StartJob(atoms) =>
//       context.become(inProgress, true)
//       processJob(atoms) onSuccess {
//         case completedCount => context.become(waiting(Some(Status.Completed(completedCount))), true)
//       }
//       sender ! Submitted
//     case GetJobStatus => sender ! lastStatus
//   }

//   def receive = waiting(None)

//   //   // def busy: Receive = {
//   //   //   case msg => println(msg)
//   //   // }

//   // }

// }

// object KinesisAtomActor {

//   case class StartJob(atoms: Iterator[Atom])
//   case object GetJobStatus

//   case object Submitted

//   sealed trait Status
//   object Status {
//     case class Completed(completedCount: Int) extends Status
//     case class Error(msg: String, completedCount: Int) extends Status
//   }

// }

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
          Thread.sleep(1000)
        }
        _isComplete = true
        _completedCount
      }
    }

  def reindexStatus = ???

}
 
