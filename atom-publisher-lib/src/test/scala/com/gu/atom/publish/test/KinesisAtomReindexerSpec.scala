package com.gu.atom.publish.test

import com.gu.atom.publish.{PublishedKinesisAtomReindexer, PreviewKinesisAtomReindexer, KinesisAtomReindexer}

// import akka.actor.{ ActorRef, ActorSystem, Props }
// import akka.testkit.ImplicitSender
// import com.gu.atom.publish._

import com.amazonaws.services.kinesis.AmazonKinesisClient
import org.scalatest.{ FunSpecLike, Matchers }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

import org.mockito.ArgumentMatchers.{ eq => meq, _ }

import com.gu.atom.TestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class KinesisAtomReindexerSpec
    extends FunSpecLike
    with Matchers
    with ScalaFutures
    with MockitoSugar {

  describe("Preview Kinesis Atom Reindexer") {

    it("should call putRecords() for each atom") {
      val kinesis = mock[AmazonKinesisClient]
      val reindexer = new PreviewKinesisAtomReindexer("testStream", kinesis)
      val expectedCount = TestData.testAtoms.size
      val job = reindexer.startReindexJob(TestData.testAtomEvents.iterator, expectedCount)
      job.expectedSize should equal(expectedCount)
      whenReady(job.execute, timeout(13.seconds)) { completedCount =>
        completedCount should equal(expectedCount)
        job.completedCount should equal(expectedCount)
        job.isComplete should equal(true)
        verify(kinesis, times(expectedCount)).putRecord(meq("testStream"), any(), meq("Media"))
      }
    }
  }

  describe("Live Kinesis Atom Reindexer") {

    it("should call putRecords() for each atom") {
      val kinesis = mock[AmazonKinesisClient]
      val reindexer = new PublishedKinesisAtomReindexer("testStream", kinesis)
      val expectedCount = TestData.testAtoms.size
      val job = reindexer.startReindexJob(TestData.testAtomEvents.iterator, expectedCount)
      job.expectedSize should equal(expectedCount)
      whenReady(job.execute, timeout(13.seconds)) { completedCount =>
        completedCount should equal(expectedCount)
        job.completedCount should equal(expectedCount)
        job.isComplete should equal(true)
        verify(kinesis, times(expectedCount)).putRecord(meq("testStream"), any(), meq("Media"))
      }
    }
  }
}
