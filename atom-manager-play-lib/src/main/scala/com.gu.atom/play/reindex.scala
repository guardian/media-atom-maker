package com.gu.atom.play

import akka.actor.{ActorRef, ActorSystem, Props, Actor}
import com.gu.atom.publish._
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import play.api.Configuration

import play.api.mvc._
import javax.inject.Inject

import com.gu.atom.data._

import javax.inject.Singleton
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import java.util.Date
import scala.concurrent.Future

import play.api.libs.json._

import ReindexActor._

/*
 * In here we find the Actor that is responsible for initialising,
 * monitoring, and finishing the reindex job.
 *
 * The controller will send it messages to get a status update and so on.
 */

class ReindexActor(reindexer: AtomReindexer) extends Actor {
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

@Singleton
class ReindexController @Inject() (
                                   previewDataStore: PreviewDataStore,
                                   publishedDataStore: PublishedDataStore,
                                   previewReindexer: PreviewAtomReindexer,
                                   publishedReindexer: PublishedAtomReindexer,
                                   config: Configuration,
                                   system: ActorSystem) extends Controller {

  def now() = (new Date()).getTime()

  implicit val ec = system.dispatcher

  val previewReindexActor = system.actorOf(Props(classOf[ReindexActor], previewReindexer))
  val publishedReindexActor = system.actorOf(Props(classOf[ReindexActor], publishedReindexer))

  implicit val timeout = Timeout(5.seconds)

  implicit val statusWrites = Json.writes[JobStatus]

  object ApiKeyAction extends ActionBuilder[Request] {
    lazy val apiKey = config.getString("reindexApiKey").get

    def invokeBlock[A](request: Request[A], block: (Request[A] => Future[Result])) = {
      if(request.getQueryString("api").filter(_ == apiKey).isDefined)
        block(request)
      else
        Future.successful(Unauthorized(""))
    }
  }

  def newPreviewReindexJob = getNewReindexJob(previewDataStore.listAtoms, previewReindexActor)
  def newPublishedReindexJob = getNewReindexJob(publishedDataStore.listAtoms, publishedReindexActor)

  def previewReindexJobStatus = getReindexJobStatus(previewReindexActor)
  def publishedReindexJobStatus = getReindexJobStatus(publishedReindexActor)

  private def getReindexJobStatus(actor: ActorRef) =  ApiKeyAction.async { implicit req =>
    (actor ? GetStatus) map {
      case None => NotFound("")
      case Some(job: JobStatus) => Ok(Json.toJson(job))
      case _ => InternalServerError("unknown-error")
    }
  }

  private def getNewReindexJob(getAtoms: previewDataStore.DataStoreResult[Iterator[Atom]], actor: ActorRef) =
    ApiKeyAction.async { implicit req =>
      getAtoms.fold(

        { err =>
          Future.successful(InternalServerError(err.toString))
        },

        { atoms =>
          val events = atoms.map(atom => ContentAtomEvent(atom, EventType.Update, now())).toList
          (actor ? CreateJob(events.iterator, events.size)) map {
            case RSuccess      => Ok("")
            case RFailure(msg) => InternalServerError(s"could't create job: $msg")
            case _ => InternalServerError("unknown error")
          }
        }

      )
    }

}
