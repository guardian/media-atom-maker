package controllers

import javax.inject._

import com.gu.pandahmac.HMACAuthActions
import com.squareup.okhttp.{OkHttpClient, Credentials, Request}
import java.util.concurrent.TimeUnit
import play.api.Configuration

class Support @Inject() (val authActions: HMACAuthActions,
                                  val conf: Configuration) extends AtomController {

  import authActions.APIAuthAction

  def previewCapiProxy(path: String) = APIAuthAction { request =>
    val httpClient = new OkHttpClient()
    httpClient.setConnectTimeout(5, TimeUnit.SECONDS)

    val resp = for {
      capiPreviewUser <- conf.getString("capi.previewUser")
      capiPreviewPassword <- conf.getString("capi.previewPassword")
      capiUrl <- conf.getString("capi.previewUrl")
    } yield {
      val url = s"$capiUrl/$path?${request.rawQueryString}"

      val req = new Request.Builder()
        .url(url)
        .header("Authorization", Credentials.basic(capiPreviewUser, capiPreviewPassword))
        .build

      httpClient.newCall(req).execute
    }

    resp match {
      case None =>
        InternalServerError("Could not construct CAPI request")
      case Some(r) if r.code() == 200 =>
        Ok(r.body.string).as(JSON)
      case Some(r) =>
        BadRequest(s"CAPI returned status: ${r.code()}")
    }
  }
}
