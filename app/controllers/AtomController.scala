package controllers

import com.gu.pandomainauth.action.AuthActions
import data.DataStores
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc._
import util.ThriftUtil._
import util.atom.MediaAtomImplicits

trait AtomController extends Controller with MediaAtomImplicits {
  val stores: DataStores
  val authActions: AuthActions

  /* if the creation of the thrift data from the request fails, reply
   * with the error. Otherwise delegate to `success`, which can avoid
   * error checking and deal with the thrift object directly. */

  def thriftResultAction[A](bodyParser: BodyParser[ThriftResult[A]])(success: Request[A] => Result):
      Action[ThriftResult[A]] =
    authActions.APIAuthAction(bodyParser) { implicit req =>
      req.body match {
        case Right(data) => success(Request(req, data)) // create new request with parsed body
        case Left(err) => InternalServerError(jsonError(err))
      }
    }

  def jsonError(msg: String): JsObject = JsObject(Seq("error" -> JsString(msg)))

}
