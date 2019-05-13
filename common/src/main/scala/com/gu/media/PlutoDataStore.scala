package com.gu.media

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.media.model.PlutoSyncMetadataMessage
import org.scanamo.syntax._
import org.scanamo.auto._
import org.scanamo.{Scanamo, Table}


class PlutoDataStore(client: AmazonDynamoDBClient, dynamoTableName: String) {

  val table = Table[PlutoSyncMetadataMessage](dynamoTableName)

  def getUploadsWithAtomId(id: String): List[PlutoSyncMetadataMessage] = {
    val atomIdIndex = table.index("atom-id")
    val results = Scanamo.exec(client)(atomIdIndex.query('atomId -> id))

    val errors = results.collect { case Left(err) => err }
    if (errors.nonEmpty) {
      throw DynamoPlutoTableException(errors.mkString(","))
    }

    results.collect { case Right(result) => result }
  }

  def get(id: String): Option[PlutoSyncMetadataMessage] = {
    val operation = table.get('id -> id)
    val result = Scanamo.exec(client)(operation)

    result.map {
      case Right(item) => item
      case Left(err) => throw DynamoPlutoTableException(err.toString)
    }
  }

  def put(item: PlutoSyncMetadataMessage) = {
    val result = Scanamo.exec(client)(table.put(item))
  }

  def delete(id: String) = {
    Scanamo.exec(client)(table.delete('id -> id))
  }

  case class DynamoPlutoTableException(err: String) extends RuntimeException(err)

}
