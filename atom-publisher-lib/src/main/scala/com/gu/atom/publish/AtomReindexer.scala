package com.gu.atom.publish

import com.gu.contentatom.thrift.ContentAtomEvent
import scala.concurrent.{ ExecutionContext, Future }

sealed trait ReindexJobStatus
case class Completed(completedCount: Int) extends ReindexJobStatus
case class InProgress(completedCount: Int) extends ReindexJobStatus
// case class Failed(reason: ReindexError, completedCount: Int) extends ReindexJobStatus

abstract class AtomReindexJob(atomEvents: Iterator[ContentAtomEvent], val expectedSize: Int) {
  protected var _isComplete: Boolean = false
  protected var _completedCount: Int = 0

  def isComplete = _isComplete
  def completedCount: Int = _completedCount

  def execute(implicit ec: ExecutionContext): Future[Int]
}

trait AtomReindexer {

  def startReindexJob(atomsToReindex: Iterator[ContentAtomEvent], expectedSize: Int): AtomReindexJob

}
