package data

import com.amazonaws.services.dynamodbv2.model.PutItemResult
import util.atom.MediaAtomImplicits

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.scanamo.error.{ DynamoReadError, TypeCoercionError }
import javax.inject.Inject
import com.gu.contentatom.thrift.Atom
import com.gu.scanamo.{ Scanamo, DynamoFormat, Table }
import com.gu.scanamo.query._
import com.twitter.scrooge.CompactThriftSerializer
import cats.data.Xor
import cats.implicits._

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException

class DynamoDataStore(dynamo: AmazonDynamoDBClient, tableName: String)
    extends DataStore
    with MediaAtomImplicits {
  @Inject() def this(awsConfig: util.AWSConfig) = this(awsConfig.dynamoDB, awsConfig.dynamoTableName)

  sealed trait DynamoResult
  implicit class DynamoPutResult(res: PutItemResult) extends DynamoResult

  case class AtomRow(
    id: String,
    version: Long,
    atom: String // atom data serialized by thrift
  )

  object AtomRow {
    def apply(atom: Atom): AtomRow = AtomRow(
      atom.id, atom.contentChangeDetails.revision, atomSerializer.toString(atom)
    )
  }

  // you can't use BinaryThriftStructSerializer from crooge-serializer
  // if Thrift is greater than 0.9.0 as they use a method that has
  // been removed (see https://github.com/twitter/scrooge/issues/203)
  // Also the JsonThriftserializer is one way only (it doesn't store
  // the Thrift type data because it uses the TSimpleJsonProtocol)

  private val atomSerializer = CompactThriftSerializer(Atom)

  private def rowToAtom(row: AtomRow): Xor[DynamoReadError, Atom] = try {
    Xor.Right(atomSerializer.fromString(row.atom))
  } catch {
    case err: org.apache.thrift.TException => Xor.Left(TypeCoercionError(err))
  }

  implicit val dynamoFormat: DynamoFormat[Atom] =
    DynamoFormat.xmap(rowToAtom _)(AtomRow.apply _)(DynamoFormat[AtomRow]) // <- just saving a new implicit here

  // useful shortcuts
  private val get  = Scanamo.get[Atom](dynamo)(tableName) _
  private val put  = Scanamo.put[Atom ](dynamo)(tableName) _

  // this should probably return an Either so we can report an error,
  // e.g. if the atom exists, but it can't be deseralised
  def getMediaAtom(id: String): Option[Atom] = get(UniqueKey(KeyEquals('id, id))) match {
    case Some(Xor.Right(atom)) => Some(atom)
    case _ => None
  }

  def createMediaAtom(atom: Atom) =
    if(get(UniqueKey(KeyEquals('id, atom.id))).isDefined)
      fail(IDConflictError)
    else
      succeed(put(atom))

  def updateMediaAtom(newAtom: Atom) = {
    val validationCheck = KeyIs('version, LT, newAtom.contentChangeDetails.revision)
    val res = (Scanamo.exec(dynamo)(Table[Atom](tableName).given(validationCheck).put(newAtom)))
    res.map(_ => ())
      .leftMap(_ => VersionConflictError(newAtom.tdata.activeVersion))
  }

  def listAtoms: DataStoreResult[TraversableOnce[Atom]] =
    Scanamo.scan[Atom](dynamo)(tableName).sequenceU.leftMap {
      _ => ReadError
    }
}
