package integration.services

import java.util.concurrent.TimeUnit

import com.squareup.okhttp.{OkHttpClient, Request, RequestBody, Response}

trait GuHttp {

  implicit val cookie: PandaCookie = PandaCookie()

  val httpClient = new OkHttpClient()
  httpClient.setConnectTimeout(5, TimeUnit.SECONDS)

  def gutoolsGet(url: String)(implicit cookie: PandaCookie): Response = {
    val req = new Request.Builder()
      .url(url)
      .addHeader("Cookie", s"${cookie.key}=${cookie.value}")
      .build()

    httpClient.newCall(req).execute()
  }

  def gutoolsPost(url: String, body: Option[RequestBody] = None)(implicit cookie: PandaCookie): Response = {
    val req = new Request.Builder()
      .url(url)
      .addHeader("Cookie", s"${cookie.key}=${cookie.value}")

    body match {
      case Some(e) => req.post(e)
      case None => req.addHeader("Content-Length", "0")
    }

    httpClient.newCall(req.build()).execute()
  }

  def gutoolsPut(url: String, body: Option[RequestBody] = None)(implicit cookie: PandaCookie): Response = {
    val req = new Request.Builder()
      .url(url)
      .addHeader("Cookie", s"${cookie.key}=${cookie.value}")

    body match {
      case Some(e) => req.put(e)
      case None => req.addHeader("Content-Length", "0")
    }

    httpClient.newCall(req.build()).execute()
  }

  def gutoolsDelete(url: String)(implicit cookie: PandaCookie): Response = {
    val req = new Request.Builder()
      .url(url)
      .addHeader("Cookie", s"${cookie.key}=${cookie.value}")
      .delete()
      .build()

    httpClient.newCall(req).execute()
  }

}
