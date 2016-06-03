package controllers

import com.gu.contentatom.thrift.{ Atom, AtomData }
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import model.ThriftUtil
import ThriftUtil.ThriftResult
import views.html.MediaAtom._
import data._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class MainApp @Inject() (dataStore: DataStore) extends AtomController {

  def index = Action {
    Ok("ok")
  }

  // takes a configured URL object and shows how it would look as a content atom

  def getMediaAtom(id: String) = Action { implicit req =>
    dataStore.getMediaAtom(id) match {
      case Some(atom) => Ok(displayAtom(atom))
      case None => NotFound(s"no atom with id $id found")
    }
  }

}
