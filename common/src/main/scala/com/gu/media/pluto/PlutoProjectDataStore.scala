package com.gu.media.pluto

import com.amazonaws.services.dynamodbv2.model.PutItemResult
import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}
import com.gu.scanamo.DynamoFormat._
import com.gu.scanamo.DynamoFormat
import org.joda.time.{DateTime, DateTimeZone}

case class PlutoProjectDataStoreException(err: String) extends Exception(err)

class PlutoProjectDataStore(aws: DynamoAccess) extends Logging {
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
    log.info(s"saving pluto project ${plutoProject.id}")
    val op = table.put(plutoProject)
    Scanamo.exec(aws.dynamoDB)(op)
  }

  def get(id: String): Option[PlutoProject] = {
    val op = table.get('id -> id)
    val result = Scanamo.exec(aws.dynamoDB)(op)

    result.map {
      case Right(project) => project
      case Left(err) => {
        log.info(s"pluto project $id not found")
        throw PlutoProjectDataStoreException(err.toString)
      }
    }
  }

  def update(id: String, plutoProject: PlutoProject): Option[PutItemResult] = {
    get(id) match {
      case Some(_) => {
        log.info(s"updating pluto project $id")
        Some(put(plutoProject))
      }
      case None => {
        log.error(s"failed to update pluto project $id as it does not exist")
        None
      }
    }
  }
}
