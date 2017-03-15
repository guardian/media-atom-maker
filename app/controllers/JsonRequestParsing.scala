package controllers

import com.gu.pandomainauth.action.UserRequest
import model.commands.CommandExceptions.commandExceptionAsResult
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.mvc.{AnyContent, Controller, Result}

trait JsonRequestParsing { this: Controller =>
  def parse[T](raw: UserRequest[AnyContent])(fn: T => Result)(implicit reads: Reads[T]): Result = try {
    raw.body.asJson match {
      case Some(rawJson) =>
        rawJson.validate[T] match {
          case JsSuccess(request, _) =>
            fn(request)

          case JsError(errors) =>
            val errorsByPath = errors.flatMap { case(p, e) => e.map(p -> _) } // flatten
            val msg = errorsByPath.map { case(p, e) => s"$p -> $e" }.mkString("\n")

            Logger.info(s"Error parsing request: $msg - ${raw.body}")
            BadRequest(msg)
        }

      case None =>
        Logger.info(s"Error parsing request: ${raw.body}")
        BadRequest("Unable to parse body as JSON")
    }
  } catch {
    commandExceptionAsResult
  }
}
