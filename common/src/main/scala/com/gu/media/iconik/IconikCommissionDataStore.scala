package com.gu.media.iconik

import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import org.scanamo.generic.auto._
import org.scanamo.syntax._
import org.scanamo.{DynamoReadError, Scanamo, Table}

case class IconikCommissionDataStoreException(err: String)
    extends Exception(err)

class IconikCommissionDataStore(aws: DynamoAccess) extends Logging {
  val scanamo: Scanamo = aws.scanamo
  private val table = Table[IconikCommission](aws.iconikCommissionTableName)
  private val workingGroupIndex = table.index("working-group-index")

  def getById(
      commissionId: String
  ): Option[Either[DynamoReadError, IconikCommission]] = {
    log.info(s"getting commission $commissionId")
    scanamo.exec(table.get("id" === commissionId))
  }

  def list(): List[IconikCommission] = {
    scanamo
      .exec(table.scan())
      .collect {
        case Left(error) =>
          log.error("failed to list iconik commissions")
          throw IconikWorkingGroupDataStoreException(error.toString)
        case Right(commission) =>
          commission
      }
      .sortBy(_.title)
  }

  def getByWorkingGroupId(workingGroupId: String): List[IconikCommission] = {
    scanamo
      .exec(workingGroupIndex.query("workingGroupId" === workingGroupId))
      .collect {
        case Left(error) =>
          log.error(
            s"failed to get iconik commissions for working group $workingGroupId"
          )
          throw IconikCommissionDataStoreException(error.toString)
        case Right(iconikCommission) =>
          iconikCommission
      }
      .sortBy(_.title)
  }

  def upsert(iconikUpsertRequest: IconikUpsertRequest): Unit = {
    val commission = IconikCommission.build(iconikUpsertRequest)
    log.info(s"upserting iconik commission ${commission.id}")
    scanamo.exec(table.put(commission))
  }

  def delete(commissionId: String): Unit = {
    log.info(s"deleting commission $commissionId")
    scanamo.exec(table.delete("id" === commissionId))
  }
}
