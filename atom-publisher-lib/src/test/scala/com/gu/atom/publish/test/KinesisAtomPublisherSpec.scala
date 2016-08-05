package com.gu.atom.publish.test

import java.nio.ByteBuffer

import com.gu.atom.publish.KinesisAtomPublisher

import com.amazonaws.services.kinesis.AmazonKinesisClient

import org.mockito.ArgumentMatchers.{eq => argEq, _}

import org.scalatest.{FunSpec, Matchers}
import org.scalatest.mock.MockitoSugar
import com.gu.atom.TestData._
import org.mockito.Mockito._


import scala.util.{Failure, Success}

class KinesisAtomPublisherSpec
    extends FunSpec
    with Matchers
    with MockitoSugar {

  val streamName = "testStream"

  describe("Kinesis Atom Publisher") {
    it("should call putRecords()") {
      val kinesis = mock[AmazonKinesisClient]
      val publisher = new KinesisAtomPublisher(streamName, kinesis)
      val res = publisher.publishAtomEvent(testAtomEvent())
      verify(kinesis).putRecord(argEq(streamName), any(classOf[ByteBuffer]), any())
      res should equal(Success(()))
    }

    it("should report exception") {
      val kinesis = mock[AmazonKinesisClient]
      when(kinesis.putRecord(argEq(streamName), any(classOf[ByteBuffer]), anyString()))
        .thenThrow(classOf[Exception])
      val publisher = new KinesisAtomPublisher(streamName, kinesis)
      val res = publisher.publishAtomEvent(testAtomEvent())
      res should matchPattern { case Failure(e: Exception) => }
    }

  }
}
