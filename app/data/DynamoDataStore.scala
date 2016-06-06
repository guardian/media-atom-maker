package data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import javax.inject.Inject
import com.gu.contentatom.thrift.Atom
import com.gu.scanamo.{ Scanamo, DynamoFormat }
import com.gu.scanamo.error.MissingProperty
import com.twitter.scrooge.BinaryThriftStructSerializer

class DynamoDataStore(dynamo: AmazonDynamoDBClient, tableName: String) extends DataStore {
  @Inject() def this(aws: util.AWS) = this(aws.dynamoDB, aws.dynamoTableName)

  private val jsonEncoder = BinaryThriftStructSerializer(Atom)

  def serializeAtom(atom: Atom): String = jsonEncoder.toString(atom)

  def getMediaAtom(id: String): Option[Atom] = ???
  def createMediaAtom(atom: Atom): Unit = {
    import scala.collection.JavaConversions._
    val data = Map(
      "id" -> atom.id,
      "atom" -> serializeAtom(atom)
    )
    Scanamo.put(dynamo)(tableName)(data)
  }
  def updateMediaAtom(newAtom: Atom): Unit = ???

}
