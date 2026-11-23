package com.gu.media.iconik

import scala.collection.mutable.{ListBuffer => MutableList}
import scala.concurrent.{ExecutionContext, Future}

class IconikInMemoryStore[T <: IconikItem, E] extends IconikDataStore[T, E] {

  val store: MutableList[T] = MutableList[T]()

  override def getById(id: String): Either[E, Option[T]] = Right(
    store.find(_.id == id)
  )

  override def list: Either[E, List[T]] = Right(store.toList)

  override def listAsync()(implicit
      executionContext: ExecutionContext
  ): Future[Either[E, List[T]]] = Future(list)

  override def upsert(item: T): Unit = {
    val maybeExistingItem = store.find(_.id == item.id)
    maybeExistingItem.foreach(existingItem => store -= existingItem)
    store += item
  }

  override def deleteById(id: String): Unit = store.filterInPlace(_.id != id)
}

class IconikInMemoryStoreWithParentIndex[T <: IconikItemWithParentId, E]
    extends IconikInMemoryStore[T, E]
    with IconikDataStoreWithParentIndex[T, E] {

  override def getByParentId(id: String): Either[E, List[T]] = Right(
    store.filter(_.parentId == id).toList
  )
}

class IconikInMemoryProjectStore[E]
    extends IconikInMemoryStoreWithParentIndex[IconikProject, E]
    with IconikProjectStore[E] {
  override def createIfAbsent(project: IconikProject): Either[E, Boolean] =
    synchronized {
      val absent = !store.exists(_.id == project.id)
      if (absent) upsert(project)
      Right(absent)
    }

  override def updateHierarchy(
      before: IconikProject,
      after: IconikProject
  ): Either[E, Boolean] =
    synchronized {
      val matching = store.find(p =>
        p.id == before.id && p.title == before.title &&
          p.commissionId == before.commissionId &&
          p.workingGroupId == before.workingGroupId
      )
      matching.foreach { current =>
        upsert(
          current.copy(
            title = after.title,
            commissionId = after.commissionId,
            workingGroupId = after.workingGroupId
          )
        )
      }
      Right(matching.isDefined)
    }
}
