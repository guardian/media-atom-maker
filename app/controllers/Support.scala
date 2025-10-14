package controllers

import com.gu.media.{CapiAccess, CapiException}
import com.gu.pandahmac.HMACAuthActions
import play.api.mvc.{Action, BaseController, ControllerComponents}

class Support(
    val authActions: HMACAuthActions,
    val capi: CapiAccess,
    val controllerComponents: ControllerComponents
) extends BaseController {
  import authActions.APIAuthAction

  def capiProxy(path: String, queryLive: Boolean) = APIAuthAction { request =>
    val qs: Map[String, Seq[String]] = request.queryString

    try {
      val result = capi.complexCapiQuery(path, qs, queryLive)
      Ok(result)
    } catch {
      case CapiException(err, _) =>
        InternalServerError(err)
    }
  }
}
