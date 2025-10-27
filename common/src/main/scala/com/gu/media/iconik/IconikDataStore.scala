package com.gu.media.iconik

import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import org.scanamo.{DynamoFormat, DynamoReadError, Scanamo, Table}
import org.scanamo.syntax._

import scala.collection.mutable.{ListBuffer => MutableList}
import java.time.Instant

abstract class IconikDataStore[Model <: IconikItem, ErrorType] {

  def getById(id: String): Either[ErrorType, Option[Model]]

  def list: Either[ErrorType, List[Model]]

  def upsert(item: Model): Unit

  def deleteById(id: String): Unit
}

trait WithParentIdIndex[
    Model <: IconikItem,
    ErrorType
] {
  def getByParentId(id: String): Either[ErrorType, List[Model]]
}

trait IconikDataStoreWithParentIndex[Model <: IconikItemWithParentId, ErrorType]
    extends IconikDataStore[Model, ErrorType]
    with WithParentIdIndex[Model, ErrorType]
