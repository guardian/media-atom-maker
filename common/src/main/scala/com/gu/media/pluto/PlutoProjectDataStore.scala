package com.gu.media.pluto

import com.amazonaws.services.dynamodbv2.model.PutItemResult
import com.gu.media.aws.DynamoAccess
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}
import com.gu.scanamo.DynamoFormat._
import com.gu.scanamo.DynamoFormat
import org.joda.time.{DateTime, DateTimeZone}

class PlutoProjectDataStore(aws: DynamoAccess) {
  implicit val dateTimeFormat = DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](
    DateTime.parse(_).withZone(DateTimeZone.UTC)
  )(_.toString)

  private val table = Table[PlutoProject](aws.plutoProjectTableName)

  def list(): List[PlutoProject] = {
    val op = table.scan()
    val results = Scanamo.exec(aws.dynamoDB)(op)

    results.collect {
      case Left(error) => {
        throw new Exception(error.toString)
      }
      case Right(plutoProject) => {
        plutoProject
      }
    }
  }

  def put(plutoProject: PlutoProject): PutItemResult = {
    val op = table.put(plutoProject)
    Scanamo.exec(aws.dynamoDB)(op)
  }

  def get(id: String): Option[PlutoProject] = {
    val op = table.get('id -> id)
    val result = Scanamo.exec(aws.dynamoDB)(op)

    result.map {
      case Right(project) => project
      case Left(err) => {
        throw new Exception(err.toString)
      }
    }
  }

  def update(id: String, plutoProject: PlutoProject): Option[PutItemResult] = {
    get(id) match {
      case Some(_) => Some(put(plutoProject))
      case None => None
    }
  }
}
