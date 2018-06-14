package com.gu.media.pluto

import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import com.gu.scanamo.{Scanamo, Table}
import com.gu.scanamo.DynamoFormat._
import com.gu.scanamo.DynamoFormat
import com.gu.scanamo.syntax._
import org.joda.time.{DateTime, DateTimeZone}

case class PlutoProjectDataStoreException(err: String) extends Exception(err)

class PlutoProjectDataStore(aws: DynamoAccess, plutoCommissionDataStore: PlutoCommissionDataStore) extends Logging {
  implicit val dateTimeFormat = DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](
    DateTime.parse(_).withZone(DateTimeZone.UTC)
  )(_.toString)

  private val table = Table[PlutoProject](aws.plutoProjectTableName)
  private val commmissionIndex = table.index("commission-index")

  def getByCommissionId(commissionId: String): List[PlutoProject] = {
    val op = commmissionIndex.query('commissionId -> commissionId)

    val results = Scanamo.exec(aws.dynamoDB)(op)

    results.collect {
      case Left(error) => {
        log.error(s"failed to get pluto projects for commission $commissionId")
        throw PlutoProjectDataStoreException(error.toString)
      }
      case Right(plutoProject) => {
        plutoProject
      }
    }.sortBy(_.title)
  }

  def upsert(plutoUpsertRequest: PlutoUpsertRequest): PlutoProject = {
    plutoCommissionDataStore.upsert(plutoUpsertRequest)

    val project = PlutoProject.build(plutoUpsertRequest)
    log.info(s"upserting pluto project ${project.id}")
    val op = table.put(project)
    Scanamo.exec(aws.dynamoDB)(op)
    project
  }

  def deleteByCommissionId(commissionId: String) = {
    log.info(s"deleting all pluto projects for commission $commissionId")
    getByCommissionId(commissionId).map(project => Scanamo.delete(aws.dynamoDB)(table.name)('id -> project.id))
  }
}
