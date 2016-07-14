package com.gu.atom.publish.test

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.ImplicitSender
import com.gu.atom.publish._

import com.amazonaws.services.kinesis.AmazonKinesisClient
import org.scalatest.{ BeforeAndAfterAll, fixture, Matchers }
import org.scalatest.mock.MockitoSugar

import akka.testkit.TestKit

import com.gu.atom.publish.KinesisAtomActor._

class KinesisAtomReindexerSpec
    extends TestKit(ActorSystem("KinesisAtomReindexerSpec"))
    with ImplicitSender
    with fixture.FunSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll {

  val kinesis = mock[AmazonKinesisClient]

  type FixtureParam = ActorRef

  def withFixture(test: OneArgTest) = {
    val actorRef = system.actorOf(Props(classOf[KinesisAtomActor], "test", kinesis))
    super.withFixture(test.toNoArgTest(actorRef))
  }

  override def afterAll = {
    shutdown(system)
  }

  describe("Kinesis Atom Reindexer") {
    it("should call putRecords() for each atom") { actor =>
      actor ! StartJob(Iterator.empty)
      expectMsg(OK)
    }
  }

}
