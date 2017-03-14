package data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.scanamo.error.DynamoReadError
import com.gu.scanamo.{DynamoFormat, Scanamo, Table}
import com.gu.scanamo.syntax._

import scala.reflect.ClassTag


class ScanamoDataStore[T: ClassTag : DynamoFormat](client: AmazonDynamoDBClient, dynamoTableName: String) {

  //TODO: filter list by id to deal with uploads datastore

  private val table = Table[T](dynamoTableName)

  private def list(): List[Either[DynamoReadError, T]] = {
    val operation = table.scan()
    val allResults = Scanamo.exec(client)(operation)

    val errors = allResults.collect { case Left(err) => err }
    if (errors.nonEmpty) {
      throw DynamoTableException(errors.mkString(","))
    }

    allResults
  }


  def listAll(): List[T] = {
    val allResults = list()
    allResults.map { case Right(upload) => upload }
  }

  def get(id: String): Option[T] = {
    val operation = table.get('id -> id)
    val result = Scanamo.exec(client)(operation)

    result.map {
      case Right(upload) => upload
      case Left(err) => throw DynamoTableException(err.toString)
    }
  }


  def delete(id: String): Unit = {
    Scanamo.exec(client)(table.delete('id -> id))
  }

  case class DynamoTableException(err: String) extends RuntimeException(err)

}
