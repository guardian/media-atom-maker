package com.gu.atom.play

import com.gu.atom.data._
import com.gu.atom.publish._
import com.gu.contentatom.thrift._
import java.util.Date
import play.api.mvc._
import scala.util.{ Failure, Success }
import play.api.libs.json.{ JsObject, JsString }

trait AtomAPIActions extends Controller {

  val livePublisher: LiveAtomPublisher
  val previewPublisher: PreviewAtomPublisher
  val dataStore: DataStore

  private def jsonError(msg: String): JsObject = JsObject(Seq("error" -> JsString(msg)))

  def publishAtom(atomId: String) = Action { implicit req =>
    dataStore.getAtom(atomId) match {
      case Some(atom) =>
        val event = ContentAtomEvent(atom, EventType.Update, (new Date()).getTime())
        livePublisher.publishAtomEvent(event) match {
          case Success(_)  => NoContent
          case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
        }
      case None => NotFound(jsonError(s"No such atom $atomId"))
    }
  }
}
