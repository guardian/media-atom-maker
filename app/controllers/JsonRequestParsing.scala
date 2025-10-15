package controllers

import com.gu.media.logging.Logging
import model.commands.CommandExceptions.commandExceptionAsResult
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.mvc.{AnyContent, BaseController, Request, Result}

trait JsonRequestParsing extends Logging { this: BaseController =>
  def parse[T](
      raw: Request[AnyContent]
  )(fn: T => Result)(implicit reads: Reads[T]): Result = try {
    raw.body.asJson match {
      case Some(rawJson) =>
        rawJson.validate[T] match {
          case JsSuccess(request, _) =>
            fn(request)

          case JsError(errors) =>
            val errorsByPath = errors.flatMap { case (p, e) =>
              e.map(p -> _)
            } // flatten
            val msg =
              errorsByPath.map { case (p, e) => s"$p -> $e" }.mkString("\n")

            log.info(s"Error parsing request: $msg - ${raw.body}")
            BadRequest(msg)
        }

      case None =>
        log.info(s"Error parsing request: ${raw.body}")
        BadRequest("Unable to parse body as JSON")
    }
  } catch {
    commandExceptionAsResult
  }
}
