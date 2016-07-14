package com.gu.atom.publish

import akka.actor.{ Actor, Props, ActorSystem }
import akka.util.Timeout
import com.gu.contentatom.thrift.Atom
import com.amazonaws.services.kinesis.AmazonKinesisClient
import akka.pattern.ask
import scala.concurrent.ExecutionContext

import scala.concurrent.duration._

import KinesisAtomActor._

class KinesisAtomActor(streamName: String, kinesis: AmazonKinesisClient)
    extends Actor {

  def receive = {
    case StartJob(atoms) => sender ! OK
  }

  //   // def busy: Receive = {
  //   //   case msg => println(msg)
  //   // }

  // }

}

object KinesisAtomActor {
  case class StartJob(atoms: Iterator[Atom])
  case object OK
}

class KinesisAtomReindexer(
  streamName: String, kinesis: AmazonKinesisClient,
  system: ActorSystem
)(implicit ec: ExecutionContext) extends AtomReindexer {

  implicit val timeout = Timeout(5.seconds)
  val actor = system.actorOf(Props(classOf[KinesisAtomActor], streamName, kinesis))

  def startReindexJob(atomsToReindex: Iterator[Atom]) = (actor ? StartJob(atomsToReindex)) map (_ => ())
  def reindexStatus = ???

}
 
