package com.gu.media.pluto

import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import com.gu.media.pluto.PlutoItem.numericIdsOnlyFilter
import org.joda.time.{DateTime, DateTimeZone}
import org.scanamo.DynamoFormat._
import org.scanamo.generic.auto._
import org.scanamo.syntax._
import org.scanamo.{DynamoFormat, DynamoReadError, Scanamo, Table}

case class PlutoProjectDataStoreException(err: String) extends Exception(err)

class PlutoProjectDataStore(
    aws: DynamoAccess,
    plutoCommissionDataStore: PlutoCommissionDataStore
) extends Logging {
  implicit val dateTimeFormat: DynamoFormat[DateTime] =
    DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](
      DateTime.parse(_).withZone(DateTimeZone.UTC),
      _.toString
    )

  val scanamo: Scanamo = aws.scanamo
  private val table = Table[PlutoProject](aws.plutoProjectTableName)
  private val commmissionIndex = table.index("commission-index")

  def getById(
      projectId: String
  ): Option[Either[DynamoReadError, PlutoProject]] = {
    log.info(s"getting project $projectId")
    scanamo.exec(table.get("id" === projectId))
  }

  def getByCommissionId(commissionId: String): List[PlutoProject] = {
    scanamo
      .exec(commmissionIndex.query("commissionId" === commissionId))
      .collect {
        case Left(error) =>
          log.error(
            s"failed to get pluto projects for commission $commissionId"
          )
          throw PlutoProjectDataStoreException(error.toString)
        case Right(plutoProject) =>
          plutoProject
      }
      .filter(numericIdsOnlyFilter)
      .sortBy(_.title)
  }

  def upsert(plutoUpsertRequest: PlutoUpsertRequest): PlutoProject = {
    plutoCommissionDataStore.upsert(plutoUpsertRequest)

    val project = PlutoProject.build(plutoUpsertRequest)
    log.info(s"upserting pluto project ${project.id}")
    scanamo.exec(table.put(project))
    project
  }

  def deleteByCommissionId(commissionId: String): Unit = {
    log.info(s"deleting all pluto projects for commission $commissionId")
    getByCommissionId(commissionId).foreach(project =>
      scanamo.exec(table.delete("id" === project.id))
    )
  }
}
