package com.gu.media.pluto

import com.amazonaws.services.dynamodbv2.model.PutItemResult
import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import com.gu.scanamo.{Scanamo, Table}

case class PlutoCommissionDataStoreException(err: String) extends Exception(err)

class PlutoCommissionDataStore(aws: DynamoAccess) extends Logging {
  private val table = Table[PlutoCommission](aws.plutoCommissionTableName)

  def list(): List[PlutoCommission] = {
    val op = table.scan()
    val results = Scanamo.exec(aws.dynamoDB)(op)

    results.collect {
      case Left(error) => {
        log.error("failed to list pluto commissions")
        throw PlutoCommissionDataStoreException(error.toString)
      }
      case Right(commission) => {
        commission
      }
    }
  }

  def upsert(plutoUpsertRequest: PlutoUpsertRequest): PutItemResult = {
    val commission = PlutoCommission.build(plutoUpsertRequest)
    log.info(s"upserting pluto commission ${commission.id}")
    val op = table.put(commission)
    Scanamo.exec(aws.dynamoDB)(op)
  }
}
