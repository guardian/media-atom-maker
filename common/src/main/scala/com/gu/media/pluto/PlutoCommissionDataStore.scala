package com.gu.media.pluto

import com.amazonaws.services.dynamodbv2.model.DeleteItemResult
import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import org.scanamo.error.DynamoReadError
import org.scanamo.syntax._
import org.scanamo.{Scanamo, Table}
import org.scanamo.auto._

case class PlutoCommissionDataStoreException(err: String) extends Exception(err)

class PlutoCommissionDataStore(aws: DynamoAccess) extends Logging {
  private val table = Table[PlutoCommission](aws.plutoCommissionTableName)

  def list(): List[PlutoCommission] = {
    val op = table.scan()
    val results = Scanamo.exec(aws.dynamoDB)(op)

    results.collect {
      case Left(error) =>
        log.error("failed to list pluto commissions")
        throw PlutoCommissionDataStoreException(error.toString)
      case Right(commission) =>
        commission
    }.sortBy(_.title)
  }

  def upsert(plutoUpsertRequest: PlutoUpsertRequest): Option[Either[DynamoReadError, PlutoCommission]] = {
    val commission = PlutoCommission.build(plutoUpsertRequest)
    log.info(s"upserting pluto commission ${commission.id}")
    val op = table.put(commission)
    Scanamo.exec(aws.dynamoDB)(op)
  }

  def delete(commissionId: String): DeleteItemResult = {
    log.info(s"deleting commission $commissionId")
    Scanamo.exec(aws.dynamoDB)(table.delete('id -> commissionId))
  }
}
