package controllers

import com.gu.contentatom.thrift.Atom
import com.gu.contentatom.thrift.atom.media.MediaAtom
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import model.ThriftUtil
import views.html.MediaAtom._

class MainApp extends Controller {

  def index = Action {
    Ok("it works")
  }

  // takes a configured URL object and shows how it would look as a content atom

  def show(uri: String) = Action { implicit req =>
    new ThriftUtil(Map("uri" -> uri)).parseRequest match {
      case Right(atom) => Ok(displayAtom(atom))
      case Left(err) => InternalServerError(s"could not parse atom data: $err\n")
    }
  }
}
