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

case class CORSable[A](origins: String*)(action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {
    val headers = request.headers.get("Origin").map { origin =>
      if (origins.contains(origin)) {
        List(
          "Access-Control-Allow-Origin" -> origin,
          "Access-Control-Allow-Credentials" -> "true"
        )
      } else {
        Nil
      }
    }
    action(request).map(_.withHeaders(headers.getOrElse(Nil): _*))
  }

  lazy val parser: BodyParser[A] = action.parser

  override def executionContext = action.executionContext
}
