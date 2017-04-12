package data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import model.Audit

import scala.collection.JavaConversions._

class AuditDataStore(client: AmazonDynamoDBClient, auditDynamoTableName: String) {
  lazy val db = new DynamoDB(client).getTable(auditDynamoTableName)

  def getAuditTrailForAtomId(id: String): List[Audit] = {
    db.query("atomId", id).map(Audit.fromItem).toList
  }

  def putAuditEvent(audit: Audit): Unit = {
    db.putItem(audit.toItem)
  }
}
