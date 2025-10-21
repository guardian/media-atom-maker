package com.gu.media.iconik

import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import org.joda.time.{DateTime, DateTimeZone}
import org.scanamo.DynamoFormat._
import org.scanamo.generic.auto._
import org.scanamo.syntax._
import org.scanamo.{DynamoFormat, DynamoReadError, Scanamo, Table}

case class IconikProjectDataStoreException(err: String) extends Exception(err)

class IconikProjectDataStore(
    aws: DynamoAccess,
    iconikCommissionDataStore: IconikCommissionDataStore,
    iconikWorkingGroupDataStore: IconikWorkingGroupDataStore
) extends Logging {
  implicit val dateTimeFormat: DynamoFormat[DateTime] =
    DynamoFormat.coercedXmap[DateTime, String, IllegalArgumentException](
      DateTime.parse(_).withZone(DateTimeZone.UTC),
      _.toString
    )

  val scanamo: Scanamo = aws.scanamo
  private val table = Table[IconikProject](aws.iconikProjectTableName)
  private val commmissionIndex = table.index("commission-index")

  def getById(
      projectId: String
  ): Option[Either[DynamoReadError, IconikProject]] = {
    log.info(s"getting project $projectId")
    scanamo.exec(table.get("id" === projectId))
  }

  def getByCommissionId(commissionId: String): List[IconikProject] = {
    scanamo
      .exec(commmissionIndex.query("commissionId" === commissionId))
      .collect {
        case Left(error) =>
          log.error(
            s"failed to get iconik projects for commission $commissionId"
          )
          throw IconikProjectDataStoreException(error.toString)
        case Right(iconikProject) =>
          iconikProject
      }
      .sortBy(_.title)
  }

  def upsert(iconikUpsertRequest: IconikUpsertRequest): IconikProject = {
    iconikCommissionDataStore.upsert(iconikUpsertRequest)
    iconikWorkingGroupDataStore.upsert(iconikUpsertRequest)

    val project = IconikProject.build(iconikUpsertRequest)
    log.info(s"upserting iconik project ${project.id}")
    scanamo.exec(table.put(project))
    project
  }

  def deleteByCommissionId(commissionId: String): Unit = {
    log.info(s"deleting all iconik projects for commission $commissionId")
    getByCommissionId(commissionId).foreach(project =>
      scanamo.exec(table.delete("id" === project.id))
    )
  }
}
