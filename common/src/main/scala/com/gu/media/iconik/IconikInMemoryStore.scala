package com.gu.media.iconik

import scala.collection.mutable.{ListBuffer => MutableList}

class IconikInMemoryStore[T <: IconikItem, E] extends IconikDataStore[T, E] {

  val store: MutableList[T] = MutableList[T]()

  override def getById(id: String): Either[E, Option[T]] = Right(
    store.find(_.id == id)
  )

  override def list: Either[E, List[T]] = Right(store.toList)

  override def upsert(item: T): Unit = store.addOne(item)

  override def deleteById(id: String): Unit = store.filterInPlace(_.id != id)
}

class IconikInMemoryStoreWithParentIndex[T <: IconikItemWithParentId, E]
    extends IconikInMemoryStore[T, E]
    with IconikDataStoreWithParentIndex[T, E] {

  override def getByParentId(id: String): Either[E, List[T]] = Right(
    store.filter(_.parentId == id).toList
  )
}
