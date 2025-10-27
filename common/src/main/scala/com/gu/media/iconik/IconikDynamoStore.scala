package com.gu.media.iconik

import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import org.scanamo.{DynamoFormat, DynamoReadError, Scanamo, Table}
import org.scanamo.syntax._

import java.time.Instant

case class IndexConfig(indexName: String, indexKey: String)

class IconikDynamoStore[DynamoTableModel <: IconikItem: DynamoFormat](
    aws: DynamoAccess,
    tableName: String
) extends IconikDataStore[DynamoTableModel, DynamoReadError]
    with Logging {

  protected val scanamo: Scanamo = aws.scanamo
  protected val table: Table[DynamoTableModel] =
    Table[DynamoTableModel](tableName)

  // @todo: double check that this format works in practice
  implicit val instantFormat: DynamoFormat[Instant] =
    DynamoFormat.coercedXmap[Instant, String, IllegalArgumentException](
      Instant.parse(_),
      _.toString
    )

  protected def sequence[E, A](
      eithers: List[Either[E, A]]
  ): Either[E, List[A]] =
    eithers.foldRight(Right(Nil): Either[E, List[A]]) {
      case (Right(a), Right(acc)) => Right(a :: acc)
      case (Left(e), _)           => Left(e)
      case (_, Left(e))           => Left(e)
    }

  def getById(id: String): Either[DynamoReadError, Option[DynamoTableModel]] = {
    val a = scanamo.exec(table.get("id" === id))
    a match {
      case Some(Right(item)) => Right(Some(item))
      case Some(Left(err))   => Left(err)
      case None              => Right(None)
    }
  }

  def list: Either[DynamoReadError, List[DynamoTableModel]] =
    sequence(
      scanamo
        .exec(table.scan())
    ).map(_.sortBy(_.title))

  def upsert(item: DynamoTableModel): Unit = {
    log.info(s"Upserting $item")
    scanamo.exec(table.put(item))
  }

  def deleteById(id: String): Unit = {
    log.info(s"Deleting item with id $id from $tableName")
    scanamo.exec(table.delete("id" === id))
  }

  def query(
      indexValue: String,
      indexConfig: Option[IndexConfig] = None
  ): Either[DynamoReadError, List[DynamoTableModel]] = {
    val query = indexConfig match {
      case Some(IndexConfig(indexName, indexKey)) =>
        table.index(indexName).query(indexKey === indexValue)
      case _ => table.query("id" === indexValue)
    }
    sequence(scanamo.exec(query)).map(_.sortBy(_.title))
  }

}

class IconikDynamoStoreWithParentIndex[
    DynamoTableModel <: IconikItemWithParentId: DynamoFormat
](aws: DynamoAccess, tableName: String, parentIndexConfig: IndexConfig)
    extends IconikDynamoStore[DynamoTableModel](
      aws = aws,
      tableName = tableName
    )
    with IconikDataStoreWithParentIndex[DynamoTableModel, DynamoReadError] {
  private val parentIdIndex = table.index(parentIndexConfig.indexName)
  override def getByParentId(
      id: String
  ): Either[DynamoReadError, List[DynamoTableModel]] = sequence(
    scanamo.exec(parentIdIndex.query(parentIndexConfig.indexKey === id))
  )
}
