package com.gu.atom.data

import com.gu.scanamo.query._
import com.gu.scanamo.request.RequestCondition

import com.gu.scanamo.DynamoFormat
import DynamoFormat.xmap

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.gu.contentatom.thrift.Flags

import cats.data.Xor

/*
 * We are using `scanamo` and `scanamo-scrooge` in this library to
 * automate the conversion of thrift data to DynamoDB. However, there
 * are some minor features that are missing from the latest versions
 * at the time of writing, so they are added here. It is likely that
 * this file can be removed once the next version of the library is
 * published (and this project has been updated to use it).
 */

object ScanamoUtil {

  implicit def seqFormat[T](implicit f: DynamoFormat[T]): DynamoFormat[Seq[T]] =
    xmap[Seq[T], List[T]](l => Xor.right(l.toSeq))(_.toList)

  implicit val flagsFormat = xmap[Flags, Option[Boolean]](o => Xor.right(Flags.apply(o)))(f => Flags.unapply(f).get)

  // joins keys with a document separator to dig into MultipleValue keys
  case class NestedKeyIs[V : DynamoFormat](keys: List[Symbol], operator: DynamoOperator, v: V)

  implicit def nestedKeyIsCondition[V : DynamoFormat] = new ConditionExpression[NestedKeyIs[V]] {
    def apply(nested: NestedKeyIs[V])(condition: Option[RequestCondition]): RequestCondition = {
      val keys = nested.keys.zipWithIndex.map {
        case (k, i) => s"condition$i"
      }

      val placeHolders = keys.map("#" + _)
      val conditionName = placeHolders.mkString(".")
      val valueName = ":" + keys.mkString("_")
      RequestCondition(
        s"$conditionName ${nested.operator.op} $valueName",
        placeHolders.zip(nested.keys.map(_.name)).toMap,
        Some(Map(valueName -> DynamoFormat[V].write(nested.v)))
      )
    }
  }
}
