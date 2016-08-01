package com.gu.atom.data

import org.scalatest.{ Matchers, FunSpec }

import cats.data.Xor

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import com.gu.scanamo.DynamoFormat
import com.gu.scanamo.scrooge.ScroogeDynamoFormat
//import com.gu.atom.data.AtomDynamoFormats._

import AtomData._
import ScroogeDynamoFormat._
import DynamoFormat._
import ScanamoUtil._

class AtomDynamoFormatsSpec extends FunSpec with Matchers {
  //implicit val shortFmt = DynamoFormat.xmap[Short, Int](i => Xor.Right(i.toShort))(_.toInt)

  val testAtomData: AtomData = AtomData.Media(MediaAtom(Nil, Some(1L), "Test media atom", Category.Feature))

  describe("atomdata dynamo format") {
    it("should convert test atom") {
      import MediaAtomDynamoFormats._

      (DynamoFormat[AtomData].read(DynamoFormat[AtomData].write(testAtomData))
         should equal(Xor.right(testAtomData)))
    }
  }
}
