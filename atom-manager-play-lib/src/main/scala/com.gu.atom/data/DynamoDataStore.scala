package data

import com.amazonaws.services.dynamodbv2.model.PutItemResult

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.scanamo.error.{ DynamoReadError, TypeCoercionError }
import javax.inject.Inject
import com.gu.contentatom.thrift.Atom
import com.gu.scanamo.{ Scanamo, DynamoFormat, Table }
import com.gu.scanamo.query._
import com.twitter.scrooge.CompactThriftSerializer
import cats.data.Xor
import cats.implicits._

import com.gu.scanamo.scrooge.ScroogeDynamoFormat._

import com.gu.atom.data._

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException

class DynamoDataStore(dynamo: AmazonDynamoDBClient, tableName: String)
    extends DataStore {

  sealed trait DynamoResult
  implicit class DynamoPutResult(res: PutItemResult) extends DynamoResult

  implicit def seqFormat[A](implicit f: DynamoFormat[List[A]]): DynamoFormat[Seq[A]] = DynamoFormat.xmap(l => Xor.right(l.toSeq))(_.toList)

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
    if(get(UniqueKey(KeyEquals('id, atom.id))).isDefined)
      fail(IDConflictError)
    else
      succeed(put(atom))

  def updateAtom(newAtom: Atom) = {
    val validationCheck = KeyIs('version, LT, newAtom.contentChangeDetails.revision)
    val res = (Scanamo.exec(dynamo)(Table[Atom](tableName).given(validationCheck).put(newAtom)))
    res.map(_ => ())
      .leftMap(_ => VersionConflictError(newAtom.contentChangeDetails.revision))
  }

  private def findAtoms: DataStoreResult[List[Atom]] =
    Scanamo.scan[Atom](dynamo)(tableName).sequenceU.leftMap {
      _ => ReadError
    }

  def listAtoms: DataStoreResult[Iterator[Atom]] = findAtoms.rightMap(_.iterator)
}
