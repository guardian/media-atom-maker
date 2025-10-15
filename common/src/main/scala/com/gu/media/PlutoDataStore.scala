package com.gu.media

import com.gu.media.model.PlutoSyncMetadataMessage
import org.scanamo.generic.auto._
import org.scanamo.syntax._
import org.scanamo.{Scanamo, Table}

class PlutoDataStore(scanamo: Scanamo, dynamoTableName: String) {

  val table = Table[PlutoSyncMetadataMessage](dynamoTableName)

  def getUploadsWithAtomId(id: String): List[PlutoSyncMetadataMessage] = {
    val atomIdIndex = table.index("atom-id")
    val results = scanamo.exec(atomIdIndex.query("atomId" === id))

    val errors = results.collect { case Left(err) => err }
    if (errors.nonEmpty) {
      throw DynamoPlutoTableException(errors.mkString(","))
    }

    results.collect { case Right(result) => result }
  }

  def get(id: String): Option[PlutoSyncMetadataMessage] = {
    val operation = table.get("id" === id)
    val result = scanamo.exec(operation)

    result.map {
      case Right(item) => item
      case Left(err)   => throw DynamoPlutoTableException(err.toString)
    }
  }

  def put(item: PlutoSyncMetadataMessage): Unit = {
    scanamo.exec(table.put(item))
  }

  def delete(id: String): Unit = {
    scanamo.exec(table.delete("id" === id))
  }

  case class DynamoPlutoTableException(err: String)
      extends RuntimeException(err)

}
