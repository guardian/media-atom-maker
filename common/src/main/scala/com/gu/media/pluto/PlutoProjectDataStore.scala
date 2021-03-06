package com.gu.media.pluto

import com.amazonaws.services.dynamodbv2.model.DeleteItemResult
import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import com.gu.media.pluto.PlutoItem.numericIdsOnlyFilter
import org.joda.time.{DateTime, DateTimeZone}
import org.scanamo.DynamoFormat._
import org.scanamo.syntax._
import org.scanamo.{DynamoFormat, Scanamo, Table}
import org.scanamo.auto._

case class PlutoProjectDataStoreException(err: String) extends Exception(err)

class PlutoProjectDataStore(aws: DynamoAccess, plutoCommissionDataStore: PlutoCommissionDataStore) extends Logging {
  implicit val dateTimeFormat = DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](
    DateTime.parse(_).withZone(DateTimeZone.UTC)
  )(_.toString)

  private val table = Table[PlutoProject](aws.plutoProjectTableName)
  private val commmissionIndex = table.index("commission-index")

  def getById(projectId: String) = {
    log.info(s"getting project $projectId")
    Scanamo.exec(aws.dynamoDB)(table.get('id -> projectId))
  }

  def getByCommissionId(commissionId: String): List[PlutoProject] = {
    val op = commmissionIndex.query('commissionId -> commissionId)

    val results = Scanamo.exec(aws.dynamoDB)(op)

    results.collect {
      case Left(error) =>
        log.error(s"failed to get pluto projects for commission $commissionId")
        throw PlutoProjectDataStoreException(error.toString)
      case Right(plutoProject) =>
        plutoProject
    }.filter(numericIdsOnlyFilter).sortBy(_.title)
  }

  def upsert(plutoUpsertRequest: PlutoUpsertRequest): PlutoProject = {
    plutoCommissionDataStore.upsert(plutoUpsertRequest)

    val project = PlutoProject.build(plutoUpsertRequest)
    log.info(s"upserting pluto project ${project.id}")
    val op = table.put(project)
    Scanamo.exec(aws.dynamoDB)(op)
    project
  }

  def deleteByCommissionId(commissionId: String): List[DeleteItemResult] = {
    log.info(s"deleting all pluto projects for commission $commissionId")
    getByCommissionId(commissionId).map(project => Scanamo.exec(aws.dynamoDB)(table.delete('id -> project.id)))
  }
}
