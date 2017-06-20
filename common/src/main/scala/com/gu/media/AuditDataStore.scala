package com.gu.media

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.{DynamoDB, PutItemOutcome}
import org.joda.time.DateTime

import scala.collection.JavaConversions._

class AuditDataStore(client: AmazonDynamoDBClient, auditDynamoTableName: String) {
  lazy val db = new DynamoDB(client).getTable(auditDynamoTableName)

  def getAuditTrailForAtomId(id: String): List[Audit] = {
    db.query("atomId", id).map(Audit.fromItem).toList
  }

  def auditPublish(atomId: String, user: String): PutItemOutcome = {
    val audit = Audit(atomId, "publish", None, DateTime.now().getMillis, user)
    db.putItem(audit.toItem)
  }

  def auditCreate(atomId: String, user: String) = {
    val audit = Audit(atomId, "create", None, DateTime.now().getMillis, user)
    db.putItem(audit.toItem)
  }

  def auditUpdate(atomId: String, user: String, description: String) = {
    val audit = Audit(atomId, "update", Some(description), DateTime.now().getMillis, user)
    db.putItem(audit.toItem)
  }
}
