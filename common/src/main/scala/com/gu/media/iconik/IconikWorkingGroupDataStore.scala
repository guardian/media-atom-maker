package com.gu.media.iconik

import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import org.scanamo.generic.auto._
import org.scanamo.syntax._
import org.scanamo.{DynamoReadError, Scanamo, Table}

case class IconikWorkingGroupDataStoreException(err: String)
    extends Exception(err)

class IconikWorkingGroupDataStore(aws: DynamoAccess) extends Logging {
  val scanamo: Scanamo = aws.scanamo
  private val table = Table[IconikWorkingGroup](aws.iconikWorkingGroupTableName)

  def getById(
      workingGroupId: String
  ): Option[Either[DynamoReadError, IconikWorkingGroup]] = {
    log.info(s"getting working group $workingGroupId")
    scanamo.exec(table.get("id" === workingGroupId))
  }

  def list(): List[IconikWorkingGroup] = {
    scanamo
      .exec(table.scan())
      .collect {
        case Left(error) =>
          log.error("failed to list iconik working groups")
          throw IconikWorkingGroupDataStoreException(error.toString)
        case Right(workingGroup) =>
          workingGroup
      }
      .sortBy(_.title)
  }

  def upsert(iconikUpsertRequest: IconikUpsertRequest): Unit = {
    val workingGroup = IconikWorkingGroup.build(iconikUpsertRequest)
    log.info(s"upserting iconik working group ${workingGroup.id}")
    scanamo.exec(table.put(workingGroup))
  }

  def delete(workingGroupId: String): Unit = {
    log.info(s"deleting working group $workingGroupId")
    scanamo.exec(table.delete("id" === workingGroupId))
  }

}
