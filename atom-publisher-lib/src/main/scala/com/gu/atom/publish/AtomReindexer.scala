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

object AtomReindexJob {
  // mostly used for testing
  def empty: AtomReindexJob = new AtomReindexJob(Iterator.empty, 0) {
    def execute(implicit ec: ExecutionContext) = Future.successful(0)
  }
}

trait AtomReindexer {

  def startReindexJob(atomsToReindex: Iterator[ContentAtomEvent], expectedSize: Int): AtomReindexJob

}

trait PreviewAtomReindexer extends AtomReindexer
trait PublishedAtomReindexer extends AtomReindexer
