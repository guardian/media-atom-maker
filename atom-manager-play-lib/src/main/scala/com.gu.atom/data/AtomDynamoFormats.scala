package com.gu.atom.data

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.gu.contentatom.thrift.{ AtomType, AtomData }
import com.gu.contentatom.thrift.atom.media.MediaAtom

import com.twitter.scrooge.ThriftStruct

import com.gu.scanamo.DynamoFormat
import com.gu.scanamo.scrooge.ScroogeDynamoFormat

object AtomDynamoFormats {

  import DynamoFormat._
  import ScroogeDynamoFormat._

  private val atomDataTypeField = "_atomDataType"

  type AtomDataWithTypeKey[A <: ThriftStruct] = (AtomType, A)

  private def writeAtomData[A <: ThriftStruct](typ: AtomType, a: A)
                           (implicit fmt: DynamoFormat[AtomDataWithTypeKey[A]]) =
    fmt.write(typ -> a)

  implicit val adf = new DynamoFormat[AtomData] {
    def read(av: AttributeValue) =
      DynamoFormat[AtomType].read(av.getM.get("_1")) flatMap {
        case AtomType.Media => DynamoFormat[MediaAtom].read(av.getM.get("_2")) map (AtomData.Media(_))
      }

    def write(data: AtomData) = data match {
      case AtomData.Media(mediaAtom: ThriftStruct) => writeAtomData(AtomType.Media, mediaAtom)
    }
  }
}
