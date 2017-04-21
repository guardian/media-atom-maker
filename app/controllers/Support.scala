package controllers

import com.gu.media.{CapiException, CapiAccess}
import com.gu.pandahmac.HMACAuthActions
import play.api.mvc.{Action, Controller}


class Support(val authActions: HMACAuthActions, val capi: CapiAccess) extends Controller {
  import authActions.APIAuthAction

  def previewCapiProxy(path: String) = APIAuthAction { request =>
    val query = s"$path?${request.rawQueryString}"

    try {
      val result = capi.capiQuery(query)
      Ok(result)
    } catch {
      case CapiException(err, _) =>
        InternalServerError(err)
    }
  }

  def liveCapiProxy(path: String) = APIAuthAction { request =>
    val query = s"$path?${request.rawQueryString}"

    try {
      val result = capi.liveCapiQuery(query)
      Ok(result)
    } catch {
      case CapiException(err, _) =>
        InternalServerError(err)
    }
  }

  def legacyVideosEndpointRedirect(path: String) = Action { request =>
    Redirect(s"/$path?${request.rawQueryString}")
  }
}
