package com.gu.atom.data

// 
// import com.gu.contentatom.thrift.atom.media.MediaAtom

import scala.language.experimental.macros

import com.gu.contentatom.thrift.AtomData
import com.twitter.scrooge.ThriftStruct
import com.gu.scanamo.DynamoFormat
import scala.reflect.macros.blackbox.Context
import com.gu.scanamo.scrooge.ScroogeDynamoFormat
import com.amazonaws.services.dynamodbv2.model.AttributeValue

import cats.data.Xor

object AtomDynamoFormats {
  implicit def atomDataDynamoFormat: DynamoFormat[AtomData] =
    macro AtomDynamoFormatsMacros.atomData
}

trait SimpleTrait {
}

class AtomDynamoFormatsMacros(val c: Context) {
  import c.universe._

  def atomData = {
    val atomDataTpe = c.weakTypeOf[AtomData].typeSymbol.asClass
    atomDataTpe.typeSignature // https://issues.scala-lang.org/browse/SI-7046

    require(atomDataTpe.isSealed && !atomDataTpe.knownDirectSubclasses.isEmpty)

    val cases = atomDataTpe.knownDirectSubclasses map { cl =>
      val argType = cl.asType
        .asClass.primaryConstructor
        .asMethod.paramLists.head.head
        .typeSignature.dealias
      cq"""${cl.name.toTermName}(data: $argType) => DynamoFormat[$argType].write(data)"""
    }
    q"""new DynamoFormat[AtomData] {
      def read(av: _root_.com.amazonaws.services.dynamodbv2.model.AttributeValue) = ???
      def write(data: AtomData) = data match {
        case ..${cases}
      }
    }"""
  }

}
