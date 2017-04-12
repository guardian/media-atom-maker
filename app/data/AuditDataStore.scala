package data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, Item}
import model.AuditEvent
import play.api.libs.json.Json

import scala.collection.JavaConversions._

class AuditDataStore(client: AmazonDynamoDBClient, auditDynamoTableName: String) {
  lazy val db = new DynamoDB(client).getTable(auditDynamoTableName)

  def getAuditTrailForAtomId(id: String): List[AuditEvent] = {
    db.query("atomId", id).map { item =>
      Json.parse(item.toJSON).as[AuditEvent]
    }.toList
  }

  def putAuditEvent(audit: AuditEvent): Unit = {
    Item.fromJSON(Json.stringify(Json.toJson(audit)))
  }
}
