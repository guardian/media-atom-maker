package controllers

import com.gu.media.{CapiException, CapiAccess}
import com.gu.pandahmac.HMACAuthActions
import play.api.mvc.{Action, Controller}


class Support(val authActions: HMACAuthActions, val capi: CapiAccess) extends Controller {
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
