package com.gu.media.iconik

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
