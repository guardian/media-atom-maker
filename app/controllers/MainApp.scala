package controllers

import play.api.mvc._
import model.ThriftUtil.parseRequest

class MainApp extends Controller {

  def index = Action {
    Ok("it works")
  }

  // takes a configured URL object and shows how it would look as a content atom

  def show = Action(BodyParsers.parse.urlFormEncoded) { implicit req =>
    parseRequest(req.body.mapValues(_.head)) match {
      case Right(atom) => Ok(atom.toString)
      case Left(err) => InternalServerError(s"could not parse atom data: $err\n")
    }
  }
}
