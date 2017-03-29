package com.gu.media

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.media.upload.model.PlutoSyncMetadata
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}


class PlutoDataStore(client: AmazonDynamoDBClient, dynamoTableName: String) {

  val table = Table[PlutoSyncMetadata](dynamoTableName)

  def getUploadsWithAtomId(id: String): List[PlutoSyncMetadata] = {
    val atomIdIndex = table.index("atom-id")
    val results = Scanamo.exec(client)(atomIdIndex.query('atomId -> id))

    val errors = results.collect { case Left(err) => err }
    if (errors.nonEmpty) {
      throw DynamoPlutoTableException(errors.mkString(","))
    }

    results.collect { case Right(result) => result }
  }

  def get(id: String): Option[PlutoSyncMetadata] = {
    val operation = table.get('id -> id)
    val result = Scanamo.exec(client)(operation)

    result.map {
      case Right(item) => item
      case Left(err) => throw DynamoPlutoTableException(err.toString)
    }
  }

  def list(): List[PlutoSyncMetadata] = {
    val operation = table.scan()
    val allResults = Scanamo.exec(client)(operation)

    val errors = allResults.collect { case Left(err) => err }
    if (errors.nonEmpty) {
      throw DynamoPlutoTableException(errors.mkString(","))
    }

    allResults.collect { case Right(result) => result}
  }

  def put(item: PlutoSyncMetadata) = {
    val result = Scanamo.put(client)(dynamoTableName)(item)
  }

  def delete(id: String) = {
    Scanamo.exec(client)(table.delete('id -> id))
  }

  case class DynamoPlutoTableException(err: String) extends RuntimeException(err)

}
