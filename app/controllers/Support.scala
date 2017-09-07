package controllers

import com.gu.media.{CapiException, CapiAccess}
import com.gu.pandahmac.HMACAuthActions
import play.api.mvc.{Action, Controller}


class Support(val authActions: HMACAuthActions, val capi: CapiAccess) extends Controller {
  import authActions.APIAuthAction

  def capiProxy(path: String, queryLive: Boolean) = APIAuthAction { request =>
    val query = s"$path?${request.rawQueryString}"

    try {
      val result = capi.capiQuery(query, queryLive)
      Ok(result)
    } catch {
      case CapiException(err, _) =>
        InternalServerError(err)
    }
  }
}
