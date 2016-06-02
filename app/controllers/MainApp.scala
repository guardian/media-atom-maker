package controllers

import javax.inject._
import play.api.mvc._
import model.ThriftUtil
import ThriftUtil.ThriftResult
import views.html.MediaAtom._
import data.DataStore
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class MainApp @Inject() (dataStore: DataStore) extends Controller {

  /* if the creation of the thrift data from the request fails, reply
   * with the error. Otherwise delegate to `success`, which can avoid
   * error checking and deal with the thrift object directly. */
  def thriftResultAction[A](bodyParser: BodyParser[ThriftResult[A]])(success: Request[A] => Result):
      Action[ThriftResult[A]] =
    Action(bodyParser) { implicit req =>
      req.body match {
        case Right(data) => success(Request(req, data)) // create new request with parsed body
        case Left(err) => InternalServerError(err)
      }
    }

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

  def createContentAtom = thriftResultAction(ThriftUtil.bodyParser) { implicit req =>
    val atom = req.body
    try {
      dataStore.createMediaAtom(atom)
      Created(atom.id).withHeaders("Location" -> s"/atom/${atom.id}")
    } catch {
      case data.IDConflictError => Conflict(s"${atom.id} already exists")
    }
  }

  def updateContentAtom = Action(ThriftUtil.bodyParser) { implicit req =>
    NotFound("unimplemented")
  }

  def addAsset(atomId: String) = thriftResultAction(ThriftUtil.assetBodyParser) { implicit req =>
    val asset = req.body
    Ok(s"would add asset to $atomId")
  }
}
