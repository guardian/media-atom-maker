package com.gu.media.pluto

import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import com.gu.media.pluto.PlutoItem.numericIdsOnlyFilter
import org.scanamo.generic.auto._
import org.scanamo.syntax._
import org.scanamo.{DynamoReadError, Scanamo, Table}

case class PlutoCommissionDataStoreException(err: String) extends Exception(err)

class PlutoCommissionDataStore(aws: DynamoAccess) extends Logging {
  val scanamo: Scanamo = aws.scanamo
  private val table = Table[PlutoCommission](aws.plutoCommissionTableName)

  def getById(
      commissionId: String
  ): Option[Either[DynamoReadError, PlutoCommission]] = {
    log.info(s"getting commission $commissionId")
    scanamo.exec(table.get("id" === commissionId))
  }

  def list(): List[PlutoCommission] = {
    scanamo
      .exec(table.scan())
      .collect {
        case Left(error) =>
          log.error("failed to list pluto commissions")
          throw PlutoCommissionDataStoreException(error.toString)
        case Right(commission) =>
          commission
      }
      .filter(numericIdsOnlyFilter)
      .sortBy(_.title)
  }

  def upsert(plutoUpsertRequest: PlutoUpsertRequest): Unit = {
    val commission = PlutoCommission.build(plutoUpsertRequest)
    log.info(s"upserting pluto commission ${commission.id}")
    scanamo.exec(table.put(commission))
  }

  def delete(commissionId: String): Unit = {
    log.info(s"deleting commission $commissionId")
    scanamo.exec(table.delete("id" === commissionId))
  }
}
