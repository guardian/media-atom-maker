package data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import javax.inject.Inject
import com.gu.contentatom.thrift.Atom
import com.gu.scanamo.{ Scanamo, DynamoFormat, Table }
import com.gu.scanamo.error.{ DynamoReadError, MissingProperty }
import com.gu.scanamo.query._
import com.twitter.scrooge.CompactThriftSerializer
import cats.data.Xor

class DynamoDataStore(dynamo: AmazonDynamoDBClient, tableName: String) extends DataStore {
  @Inject() def this(aws: util.AWS) = this(aws.dynamoDB, aws.dynamoTableName)

  // you can't use BinaryThriftStructSerializer from crooge-serializer
  // if Thrift is greater than 0.9.0 as they use a method that has
  // been removed (see https://github.com/twitter/scrooge/issues/203)
  // Also the JsonThriftserializer is one way only (it doesn't store
  // the Thrift type data because it uses the TSimpleJsonProtocol)

  private val atomSerializer = CompactThriftSerializer(Atom)

  private def atomToMap(atom: Atom): Map[String, String] = Map(
    "id" -> atom.id,
    "atom" -> atomSerializer.toString(atom)
  )

  private def mapToAtom(map: Map[String, String]): Xor[DynamoReadError, Atom] = map.get("atom") match {
    case Some(atomData) => Xor.right(atomSerializer.fromString(atomData))
    case None => Xor.left(MissingProperty)
  }

  implicit val dynamoFormat: DynamoFormat[Atom] = DynamoFormat.xmap(mapToAtom _)(atomToMap _)

  // useful shortcuts
  private val get = Scanamo.get[Atom](dynamo)(tableName) _
  private val put = Scanamo.put[Atom](dynamo)(tableName) _

  val atomsTbl = Table[Atom](tableName)

  // this should probably return an Either so we can report an error,
  // e.g. if the atom exists, but it can't be deseralised
  def getMediaAtom(id: String): Option[Atom] = get(UniqueKey(KeyEquals('id, id))) match {
    case Some(Xor.Right(atom)) => Some(atom)
    case _ => None
  }

  def createMediaAtom(atom: Atom): Unit =
    if(get(UniqueKey(KeyEquals('id, atom.id))).isDefined)
      throw IDConflictError
    else
      put(atom)

  def updateMediaAtom(newAtom: Atom): Unit = ???

}
