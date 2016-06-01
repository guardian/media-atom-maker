package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import model.ThriftUtil

class MainApp extends Controller {

  def index = Action {
    Ok("it works")
  }

  // takes a configured URL object and shows how it would look as a content atom

  def show = Action(ThriftUtil.bodyParser) { implicit req =>
    req.body match {
      case Right(atom) => Ok(atom.toString)
      case Left(err) => InternalServerError(s"could not parse atom data: $err\n")
    }
  }
}
