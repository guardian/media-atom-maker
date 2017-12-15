package controllers

import com.gu.editorial.permissions.client.{Permission, PermissionGranted, PermissionsUser}
import com.gu.media.Permissions._
import com.gu.media.{MediaAtomMakerPermissionsProvider, Permissions}
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.action.UserRequest
import com.gu.pandomainauth.model.User
import data.UnpackedDataStores
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc._
import com.gu.media.util.ThriftUtil._

import scala.concurrent.Future

trait AtomController extends Controller with UnpackedDataStores {
  val authActions: HMACAuthActions
  val permissions: MediaAtomMakerPermissionsProvider

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

  import authActions.APIAuthAction

  object LookupPermissions extends ActionBuilder[UploadUserRequest] {
    override def invokeBlock[A](request: Request[A], block: UploadUserRequest[A] => Future[Result]): Future[Result] = {
      APIAuthAction.invokeBlock(request, { req: UserRequest[A] =>
        permissions.getAll(req.user).flatMap { p =>
          block(new UploadUserRequest(req.user, request, p))
        }
      })
    }
  }

  class PermissionedAction(permission: Permission) extends ActionBuilder[UserRequest] {
    override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
      APIAuthAction.invokeBlock(request, { req: UserRequest[A] =>

          permissions.get(permission)(PermissionsUser(req.user.email)).flatMap {
            case PermissionGranted =>
              block(req)

            case _ =>
              Future.successful(Unauthorized(s"User ${req.user.email} is not authorised for permission ${permission.name}"))

        }
      })
    }
  }


  val CanDeleteAtom: ActionBuilder[UserRequest] = new PermissionedAction(deleteAtom)

  case class UploadUserRequest[A](val user: User, request: Request[A], permissions: Permissions) extends WrappedRequest[A](request)

}
