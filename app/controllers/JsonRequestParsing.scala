package controllers

import model.commands.CommandExceptions.commandExceptionAsResult
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.mvc.{AnyContent, Controller, Request, Result}

import scala.concurrent.Future
import scala.util.control.NonFatal

trait JsonRequestParsing { this: Controller =>
  def parse[T](raw: Request[AnyContent])(fn: T => Result)(implicit reads: Reads[T]): Result = try {
    parseRequest(raw) match {
      case Left(err) => BadRequest(err)
      case Right(value) => fn(value)
    }
  } catch {
    commandExceptionAsResult
  }

  def parseAsync[T](raw: Request[AnyContent])(fn: T => Future[Result])(implicit reads: Reads[T]): Future[Result] = try {
    parseRequest(raw) match {
      case Left(err) => Future.successful(BadRequest(err))
      case Right(value) => fn(value)
    }
  } catch {
    case NonFatal(e) =>
      Future.successful {
        commandExceptionAsResult.applyOrElse(e, { e: Throwable =>
          Logger.error(e.getMessage, e)
          InternalServerError(e.getMessage)
        })
      }
  }

  private def parseRequest[T](raw: Request[AnyContent])(implicit reads: Reads[T]): Either[String, T] = {
    raw.body.asJson match {
      case Some(rawJson) =>
        rawJson.validate[T] match {
          case JsSuccess(request, _) =>
            Right(request)

          case JsError(errors) =>
            val errorsByPath = errors.flatMap { case(p, e) => e.map(p -> _) } // flatten
          val msg = errorsByPath.map { case(p, e) => s"$p -> $e" }.mkString("\n")

            Logger.info(s"Error parsing request: $msg - ${raw.body}")
            Left(msg)
        }

      case None =>
        Logger.info(s"Error parsing request: ${raw.body}")
        Left("Unable to parse body as JSON")
    }
  }
}
