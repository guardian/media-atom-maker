package com.gu.media

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.media.model.VideoUpload
import com.gu.scanamo.error.DynamoReadError
import com.gu.scanamo.ops.ScanamoOps
import com.gu.scanamo.syntax._
import com.gu.scanamo.{DynamoFormat, Scanamo, Table}

import scala.reflect.ClassTag


class ScanamoDataStore[T: ClassTag : DynamoFormat](client: AmazonDynamoDBClient, dynamoTableName: String) {

  //TODO: filter list by id to deal with uploads datastore

  val table = Table[T](dynamoTableName)

  def list(): List[T] = {
    val operation = table.scan()
    val allResults = Scanamo.exec(client)(operation)

    val errors = allResults.collect { case Left(err) => err }
    if (errors.nonEmpty) {
      throw DynamoTableException(errors.mkString(","))
    }

    allResults.collect { case Right(result) => result}
  }

  def get(id: String): Option[T] = {
    val operation = table.get('id -> id)
    val result = Scanamo.exec(client)(operation)

    result.map {
      case Right(item) => item
      case Left(err) => throw DynamoTableException(err.toString)
    }
  }

  def put(item: T) = {
    val result = Scanamo.put(client)(dynamoTableName)(item)
  }


  case class DynamoTableException(err: String) extends RuntimeException(err)

}

case class PlutoDataStore(client: AmazonDynamoDBClient, dynamoTableName: String)
  extends ScanamoDataStore[VideoUpload] (client, dynamoTableName) {

  def getUploadsWithAtomId(id: String): List[VideoUpload] = {
    val atomIdIndex = table.index("atom-id")
    val results = Scanamo.exec(client)(atomIdIndex.query('atomId -> id))

    val errors = results.collect { case Left(err) => err }
    if (errors.nonEmpty) {
      throw DynamoTableException(errors.mkString(","))
    }

    results.collect { case Right(result) => result }
  }

  def deleteAllWithAtom(id: String): Unit = {

    getUploadsWithAtomId(id).map(result => Scanamo.exec(client)(table.delete('id -> result.id)))

  }
}

