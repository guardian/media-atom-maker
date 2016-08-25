package com.gu.atom.data

import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, PutItemResult }
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.contentatom.thrift.{ Atom, AtomData, Flags }
import com.gu.scanamo.{ Scanamo, DynamoFormat, Table }
import com.gu.scanamo.query._
import cats.data.Xor
import cats.implicits._
import scala.reflect.ClassTag
import com.twitter.scrooge.ThriftStruct

import DynamoFormat._
import com.gu.scanamo.scrooge.ScroogeDynamoFormat._

import AtomData._

import com.gu.atom.data._

import ScanamoUtil._

abstract class DynamoDataStore[D : ClassTag : DynamoFormat]
  (dynamo: AmazonDynamoDBClient, tableName: String)
    extends DataStore
    with AtomDynamoFormats[D] {

  sealed trait DynamoResult
  implicit class DynamoPutResult(res: PutItemResult) extends DynamoResult

  // useful shortcuts
  private val get  = Scanamo.get[Atom](dynamo)(tableName) _
  private val put  = Scanamo.put[Atom](dynamo)(tableName) _

  // this should probably return an Either so we can report an error,
  // e.g. if the atom exists, but it can't be deseralised
  def getAtom(id: String): Option[Atom] = get(UniqueKey(KeyEquals('id, id))) match {
    case Some(Xor.Right(atom)) => Some(atom)
    case _ => None
  }

  def createAtom(atom: Atom) =
    if (get(UniqueKey(KeyEquals('id, atom.id))).isDefined)
      fail(IDConflictError)
    else
      succeed(put(atom))


  private def findAtoms(tableName: String): DataStoreResult[List[Atom]] =
    Scanamo.scan[Atom](dynamo)(tableName).sequenceU.leftMap {
      _ => ReadError
    }

  def listAtoms: DataStoreResult[Iterator[Atom]] = findAtoms(tableName).rightMap(_.iterator)
}

abstract class PreviewDynamoDataStore[D : ClassTag : DynamoFormat]
(dynamo: AmazonDynamoDBClient, tableName: String)
  extends DynamoDataStore[D](dynamo, tableName)
  with PreviewDataStore {

  def updateAtom(newAtom: Atom) = {
    val validationCheck = NestedKeyIs(
      List('contentChangeDetails, 'revision), LT, newAtom.contentChangeDetails.revision
    )
    val res = (Scanamo.exec(dynamo)(Table[Atom](tableName).given(validationCheck).put(newAtom)))
    res.map(_ => ())
      .leftMap(_ => VersionConflictError(newAtom.contentChangeDetails.revision))
  }

}

abstract class PublishedDynamoDataStore[D : ClassTag : DynamoFormat]
(dynamo: AmazonDynamoDBClient, tableName: String)
  extends DynamoDataStore[D](dynamo, tableName)
  with PublishedDataStore {

  def updateAtom(newAtom: Atom) =
    succeed((Scanamo.exec(dynamo)(Table[Atom](tableName).put(newAtom))))
}
