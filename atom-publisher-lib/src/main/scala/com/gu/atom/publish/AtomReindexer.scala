package com.gu.atom.publish

import com.gu.contentatom.thrift.ContentAtomEvent
import scala.concurrent.{ ExecutionContext, Future }

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
