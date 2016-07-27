package com.gu.atom.data

//
import com.gu.contentatom.thrift.atom.media.MediaAtom

import scala.language.experimental.macros
import scala.reflect.ClassTag
import com.gu.contentatom.thrift.AtomData
import com.twitter.scrooge.ThriftStruct
import com.gu.scanamo.DynamoFormat
import scala.reflect.macros.blackbox.Context
import com.gu.scanamo.scrooge.ScroogeDynamoFormat
import com.amazonaws.services.dynamodbv2.model.AttributeValue

import cats.data.Xor

import ScroogeDynamoFormat._
import DynamoFormat._

trait AtomDynamoFormats[A] {

  def fromAtomData: PartialFunction[AtomData, A]
  def toAtomData(a: A): AtomData

  def fallback(atomData: AtomData): AttributeValue =
    new AttributeValue().withS(s"unknown atom data type $atomData")

  implicit def atomDataDynamoFormat(
    implicit ct: ClassTag[A], arg0: DynamoFormat[A]
  ) =
    new DynamoFormat[AtomData] {
      def write(atomData: AtomData): AttributeValue = {
        val pf = fromAtomData andThen { case data: A => arg0.write(data) }
        pf.applyOrElse(atomData, fallback _)
      }

      def read(attr: AttributeValue) = arg0.read(attr) map (toAtomData _)

    }
}

trait MediaAtomDynamoFormats extends AtomDynamoFormats[MediaAtom] {

  def fromAtomData = { case AtomData.Media(data) => data }
  def toAtomData(data: MediaAtom) = AtomData.Media(data)

}

object MediaAtomDynamoFormats extends MediaAtomDynamoFormats

// object AtomDynamoFormats {
//   implicit def atomDataDynamoFormat: DynamoFormat[AtomData] =
//     macro AtomDynamoFormatsMacros.atomData
// }

// class AtomDynamoFormatsMacros(val c: Context) {
//   import c.universe._

//   def atomData = {
//     val atomDataTpe = c.weakTypeOf[AtomData].typeSymbol.asClass
//     atomDataTpe.typeSignature // https://issues.scala-lang.org/browse/SI-7046
//     require(atomDataTpe.isSealed && !atomDataTpe.knownDirectSubclasses.isEmpty)
//     val cases = atomDataTpe.knownDirectSubclasses map { cl =>
//       val argType = cl.asType
//         .asClass.primaryConstructor
//         .asMethod.paramLists.head.head
//         .typeSignature.dealias
//       cq"""${cl.name.toTermName}(data: $argType) => DynamoFormat[$argType].write(data)"""
//     }
//     q"""new DynamoFormat[AtomData] {
//       def read(av: _root_.com.amazonaws.services.dynamodbv2.model.AttributeValue) = ???
//       def write(data: AtomData) = data match {
//         case ..${cases}
//       }
//     }"""
//   }

// }
