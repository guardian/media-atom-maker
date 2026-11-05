package util

import com.gu.pandahmac.HMACAuthActions
import play.api.mvc.{
  Action,
  AnyContent,
  BodyParser,
  ControllerComponents,
  Request,
  Result
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object CORSable {
  def allowedOrigin(
      origin: Option[String],
      allowlist: Seq[String]
  ): Option[String] =
    origin.filter(o => allowlist.contains(o))
}

case class CORSable[A](origins: String*)(action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {
    val headers = CORSable
      .allowedOrigin(request.headers.get("Origin"), origins)
      .toList
      .flatMap(origin =>
        List(
          "Access-Control-Allow-Origin" -> origin,
          "Access-Control-Allow-Credentials" -> "true"
        )
      )
    action(request).map(
      _.withHeaders(headers: _*).withHeaders("Vary" -> "Origin")
    )
  }

  lazy val parser: BodyParser[A] = action.parser

  override def executionContext = action.executionContext
}
