package controllers

import com.gu.editorial.permissions.client.{Permission, PermissionGranted, PermissionsProvider, PermissionsUser}
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandahmac.HMACHeaderNames._
import com.gu.pandomainauth.action.UserRequest
import data.UnpackedDataStores
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc._
import util.ThriftUtil._
import play.api.libs.concurrent.Execution.Implicits._
import com.gu.media.Permissions._
import model.commands.Command

import scala.concurrent.Future

trait AtomController extends Controller with UnpackedDataStores {
  val authActions: HMACAuthActions
  val permissions: PermissionsProvider

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

  type RequestHandler[A] = UserRequest[A] => Future[Result]

  import authActions.APIHMACAuthAction

  class PermissionedAction(permission: Permission, allowHmac: Boolean) extends ActionBuilder[UserRequest] {
    override def invokeBlock[A](request: Request[A], block: RequestHandler[A]): Future[Result] = {
      APIHMACAuthAction.invokeBlock(request, { req: UserRequest[A] =>
        // At this point, an HMAC request has already been authenticated by Panda.
        // We are merely checking to see if the request was made using HMAC or not.
        val headers = request.headers.toSimpleMap.keySet
        val hmacHeaders = Set(hmacKey, dateKey, serviceNameKey)

        if (hmacHeaders.subsetOf(headers)) {
          block(req)
        } else {
          permissions.get(permission)(PermissionsUser(req.user.email)).flatMap {
            case PermissionGranted =>
              block(req)

            case _ =>
              Future.successful(Unauthorized)
          }
        }
      })
    }
  }


  val CanAddAsset: ActionBuilder[UserRequest] = new PermissionedAction(addAsset, allowHmac = true)
  val CanDeleteAtom: ActionBuilder[UserRequest] = new PermissionedAction(deleteAtom, allowHmac = false)

  def execute(command: Command)(resp: command.T => Result): Result = {
    val (result, event) = command.process()

    auditDataStore.putAuditEvent(event)
    resp(result)
  }
}
