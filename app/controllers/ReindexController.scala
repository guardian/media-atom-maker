package controllers

import akka.actor.{ ActorSystem, Props }
import com.gu.atom.publish.AtomReindexer
import com.gu.contentatom.thrift.{ ContentAtomEvent, EventType }
import play.api.Configuration

import play.api.mvc._
import javax.inject.Inject

import data._
import util._

import javax.inject.Singleton

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import util.ReindexActor._
import java.util.Date
import scala.concurrent.Future

import play.api.libs.json.{ util => _, _ } // util shadows our package util

@Singleton
class ReindexController @Inject() (dataStore: DataStore,
                                   reindexer: AtomReindexer,
                                   config: Configuration,
                                   system: ActorSystem) extends Controller {

  def now() = (new Date()).getTime()

  implicit val ec = system.dispatcher

  val reindexActor = system.actorOf(Props(classOf[ReindexActor], reindexer))

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

  def newReindexJob = ApiKeyAction.async { implicit req =>
    dataStore.listAtoms.fold(

      { err =>
        Future.successful(InternalServerError(err.toString))
      },

      { atoms =>
        val events = atoms.map(atom => ContentAtomEvent(atom, EventType.Update, now())).toList
        (reindexActor ? CreateJob(events.iterator, events.size)) map {
          case RSuccess      => Ok("")
          case RFailure(msg) => InternalServerError(s"could't create job: $msg")
          case _ => InternalServerError("unknown error")
        }
      }

    )
  }

  def reindexJobStatus = ApiKeyAction.async { implicit req =>
    (reindexActor ? GetStatus) map {
      case None => NotFound("")
      case Some(job: JobStatus) => Ok(Json.toJson(job))
      case _ => InternalServerError("unknown-error")
    }
  }

}
