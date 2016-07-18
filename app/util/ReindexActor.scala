package util

import scala.util.Success
import akka.actor.Actor
import com.gu.atom.publish.{ AtomReindexJob, AtomReindexer }
import com.gu.contentatom.thrift.ContentAtomEvent

/*
 * In here we find the Actor that is responsible for initialising,
 * monitoring, and finishing the reindex job.
 *
 * The controller will send it messages to get a status update and so on.
 */

class ReindexActor(reindexer: AtomReindexer) extends Actor {
  import ReindexActor._

  implicit val ec = context.dispatcher

  /* this is the initial, idle state. In this state we will accept new jobs */
  def idleState(lastJob: Option[AtomReindexJob]): Receive = {
    case CreateJob(atoms, expectedSize) =>
      val job = reindexer.startReindexJob(atoms, expectedSize)
      context.become(inProgressState(job))
      job.execute.onComplete {
        case _ => context.become(idleState(Some(job)), true)
      }
      sender ! RSuccess

    case GetStatus =>
      sender ! lastJob.map(statusReply _)
  }

  def inProgressState(job: AtomReindexJob): Receive = {
    case CreateJob(_, _) =>
      sender ! RFailure("in progress")
    case GetStatus =>
      sender ! Some(statusReply(job))
  }

  /* start off in idle state with no record of a previous job */
  def receive = idleState(None)

}

/* the messages that we will send and recieve */
object ReindexActor {
  /* requests */
  case class CreateJob(atoms: Iterator[ContentAtomEvent], expectedSize: Int)
  case object GetStatus

  /* responses */
  case object RSuccess
  case class RFailure(reason: String)

  /* matches response expected by CAPI */
  object StatusType extends Enumeration {
    val inProgress = Value("in progress")
    val failed     = Value("failed")
    val completed  = Value("completed")
    val cancelled  = Value("cancelled")
  }

  case class JobStatus(
    status: StatusType.Value,
    documentsIndexed: Int,
    documentsExpected: Int
  )

  def statusReply(job: AtomReindexJob): JobStatus =
    JobStatus(
      if(job.isComplete) StatusType.completed else StatusType.inProgress,
      job.completedCount,
      job.expectedSize
    )
}
