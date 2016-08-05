package com.gu.atom.play

import cats.data.Xor
import com.gu.atom.data._
import com.gu.atom.publish._
import com.gu.contentatom.thrift._
import java.util.Date
import play.api.mvc._
import scala.util.{ Failure, Success }
import play.api.libs.json.{ JsObject, JsString }

import com.gu.atom.util.AtomImplicitsGeneral._

trait AtomAPIActions extends Controller {

  val livePublisher: LiveAtomPublisher
  val previewPublisher: PreviewAtomPublisher
  val dataStore: DataStore

  private def jsonError(msg: String): JsObject = JsObject(Seq("error" -> JsString(msg)))

  def publishAtom(atomId: String) = Action { implicit req =>
    dataStore.getAtom(atomId) match {
      case Some(atom) =>
        val updatedAtom = atom.copy(
          contentChangeDetails = atom.contentChangeDetails.copy(
            published = Some(ChangeRecord((new Date()).getTime(), None))
          )
        ).bumpRevision
        val event = ContentAtomEvent(updatedAtom, EventType.Update, (new Date()).getTime())
        livePublisher.publishAtomEvent(event) match {
          case Success(_)  =>
            dataStore.updateAtom(updatedAtom) match {
              case Xor.Right(_)  => NoContent
              case Xor.Left(err) => InternalServerError(
                jsonError(s"could not update after publish: ${err.toString}")
              )
            }
          case Failure(err) => InternalServerError(jsonError(s"could not publish: ${err.toString}"))
        }
      case None => NotFound(jsonError(s"No such atom $atomId"))
    }
  }
}
