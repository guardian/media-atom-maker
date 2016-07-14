package com.gu.atom.publish

import com.gu.contentatom.thrift.Atom
import scala.concurrent.Future

// sealed trait ReindexResult[A]
// sealed abstract class ReindexError(msg: String) extends ReindexResult
// case object JobAlreadyInProgress
//     extends ReindexError("A job is already in progress")
// case class Success[A](value: A) extends ReindexResult

sealed trait ReindexJobStatus
case class Completed(completedCount: Int) extends ReindexJobStatus
case class InProgress(completedCount: Int) extends ReindexJobStatus
// case class Failed(reason: ReindexError, completedCount: Int) extends ReindexJobStatus

trait AtomReindexer {

  def startReindexJob(atomsToReindex: Iterator[Atom]): Future[Unit]
  def reindexStatus: Future[ReindexJobStatus]

}
