package controllers

import javax.inject._
import play.api.mvc._
import model.ThriftUtil
import views.html.MediaAtom._
import data.DataStore
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class MainApp @Inject() (dataStore: DataStore) extends Controller {

  def index = Action {
    Ok("hello")
  }

  // takes a configured URL object and shows how it would look as a content atom

  def getMediaAtom(id: String) = Action { implicit req =>
    dataStore.getMediaAtom(id) match {
      case Some(atom) => Ok(displayAtom(atom))
      case None => NotFound(s"no atom with id $id found")
    }
  }

  def createContentAtom = Action(ThriftUtil.bodyParser) { implicit req =>
    req.body match {
      case Right(atom) =>
        try {
          dataStore.createMediaAtom(atom)
          Created(atom.id).withHeaders("Location" -> s"/atom/${atom.id}")
        } catch {
          case data.IDConflictError => Conflict(s"${atom.id} already exists")
        }
      case Left(err) => InternalServerError(s"could not parse atom data: $err\n")
    }
  }

  def updateContentAtom = Action(ThriftUtil.bodyParser) { implicit req =>
    NotFound("unimplemented")
  }
}
