package util

import io.sentry.{
  BaggageHeader,
  ITransaction,
  Sentry,
  SentryTraceHeader,
  SpanStatus,
  TransactionContext,
  TransactionOptions
}
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader}
import play.api.routing.Router

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

/** Opens a Sentry transaction per request so server-side performance data lines
  * up with the browser traces. Continues the trace from the incoming
  * `sentry-trace`/`baggage` headers when the frontend sends them, so a single
  * trace spans browser and server.
  *
  * Sampling is governed by `tracesSampleRate` in [[SentryConfig]].
  */
class SentryTracingFilter(sentry: SentryConfig)(implicit
    ec: ExecutionContext
) extends EssentialFilter {

  private val Operation = "http.server"

  /** The load balancer polls `/healthcheck` continuously, and every page load
    * pulls many static assets. Disable tracing for these routes.
    */
  private def shouldTrace(request: RequestHeader): Boolean =
    request.path != "/healthcheck" && !request.path.startsWith("/assets/")

  override def apply(next: EssentialAction): EssentialAction =
    EssentialAction { request =>
      if (!sentry.enabled || !shouldTrace(request)) {
        next(request)
      } else {
        val transaction = startTransaction(request)
        val requestWithTransaction = request.addAttr(
          SentryTracingFilter.transactionKey,
          transaction
        )

        next(requestWithTransaction)
          .map { result =>
            transaction.setStatus(
              SpanStatus.fromHttpStatusCode(
                result.header.status,
                SpanStatus.UNKNOWN_ERROR
              )
            )
            transaction.finish()
            result
          }
          .recoverWith { case error =>
            transaction.setThrowable(error)
            transaction.setStatus(SpanStatus.INTERNAL_ERROR)
            transaction.finish()
            Future.failed(error)
          }
      }
    }

  /** Use the route pattern (e.g. `/api/atoms/:id`) rather than the resolved
    * path, otherwise every distinct id becomes its own transaction name and
    * Sentry's grouping is useless.
    */
  private def startTransaction(request: RequestHeader) = {
    val name = SentryTracingFilter.transactionName(request)

    val context = Option(
      Sentry.continueTrace(
        request.headers.get(SentryTraceHeader.SENTRY_TRACE_HEADER).orNull,
        request.headers.getAll(BaggageHeader.BAGGAGE_HEADER).toList.asJava
      )
    ).getOrElse(new TransactionContext(name, Operation))

    context.setName(name)
    context.setOperation(Operation)

    // Deliberately not bound to the global scope. Play may resume a Future on a
    // different thread, so the transaction is carried explicitly on the
    // immutable request instead.
    Sentry.startTransaction(context, new TransactionOptions())
  }
}

object SentryTracingFilter {

  val transactionKey =
    play.api.libs.typedmap.TypedKey[ITransaction]("sentry.transaction")

  /** Shared with [[RequestLogging]] so a transaction and an error raised by the
    * same request carry the same name.
    */
  def transactionName(request: RequestHeader): String =
    request.attrs
      .get(Router.Attrs.HandlerDef)
      .map(handler => s"${request.method} ${handler.path}")
      .getOrElse(s"${request.method} <unrouted>")
}
