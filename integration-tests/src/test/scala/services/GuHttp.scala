package integration.services

import java.util.concurrent.TimeUnit

import okhttp3._

trait GuHttp {

  implicit val cookie: PandaCookie = PandaCookie()

  val httpClient = new OkHttpClient()
  httpClient.setConnectTimeout(5, TimeUnit.SECONDS)

  val emptyBody: RequestBody = RequestBody.create(null, Array(192, 168, 1, 1).map(_.toByte))
  def jsonBody(bodyString: String): RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyString)

  def gutoolsGet(url: String)(implicit cookie: PandaCookie): Response = {
    val req = new Request.Builder()
      .url(url)
      .addHeader("Cookie", s"${cookie.key}=${cookie.value}")
      .build()

    httpClient.newCall(req).execute()
  }

  def gutoolsPost(url: String, body: RequestBody, headers: Map[String, String] = Map.empty)(implicit cookie: PandaCookie): Response = {
    val req = new Request.Builder()
      .url(url)
      .addHeader("Cookie", s"${cookie.key}=${cookie.value}")
      .post(body)

    headers.foreach { case(k, v) =>
      req.addHeader(k, v)
    }

    httpClient.newCall(req.build()).execute()
  }

  def gutoolsPut(url: String, body: Option[RequestBody] = None)(implicit cookie: PandaCookie): Response = {
    val req = new Request.Builder()
      .url(url)
      .addHeader("Cookie", s"${cookie.key}=${cookie.value}")

    body match {
      case Some(e) => req.put(e)
      case None => {
        req.addHeader("Content-Length", "0")
        req.put(emptyBody)
      }
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
