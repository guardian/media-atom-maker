import org.scalatest.FunSpec

import cats.data.Xor

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media.MediaAtom
import com.gu.scanamo.DynamoFormat
import com.gu.scanamo.scrooge.ScroogeDynamoFormat
import com.gu.atom.data.AtomDynamoFormats._

import AtomData._
import ScroogeDynamoFormat._
import DynamoFormat._

class AtomManagerMacrosTest extends FunSpec {
  implicit val shortFmt = DynamoFormat.xmap[Short, Int](i => Xor.Right(i.toShort))(_.toInt)
  describe("dynamo format macro") {
    val fmt = DynamoFormat[AtomData]
    println(fmt)
  }
}
