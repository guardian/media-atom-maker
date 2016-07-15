package controllers

import akka.actor.{ ActorSystem, Props }
import com.gu.atom.publish.AtomReindexer

import play.api.mvc._
import javax.inject.Inject

import data._
import util._

import javax.inject.Singleton

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import util.ReindexActor._

import play.api.libs.json.{ util => _, _ } // util shadows our package util

@Singleton
class ReindexController @Inject() (dataStore: DataStore,
                                   reindexer: AtomReindexer,
                                   system: ActorSystem) extends Controller {

  implicit val ec = system.dispatcher

  val reindexActor = system.actorOf(Props(classOf[ReindexActor], reindexer))

  implicit val timeout = Timeout(5.seconds)

  implicit val statusWrites = Json.writes[JobStatus]

  def newReindexJob = Action.async { implicit req =>
    (reindexActor ? CreateJob(Iterator.empty, 0)) map {
      case RSuccess      => Ok("")
      case RFailure(msg) => InternalServerError(s"could't create job: $msg")
      case _ => InternalServerError("unknown error")
    }
  }

  def reindexJobStatus = Action.async { implicit req =>
    (reindexActor ? GetStatus) map {
      case None => NotFound("")
      case Some(job: JobStatus) => Ok(Json.toJson(job))
      case _ => InternalServerError("unknown-error")
    }
  }

}
