package util

import com.gu.media.logging.Logging
import net.logstash.logback.marker.Markers
import org.apache.pekko.stream.Materializer
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

class RequestLoggingFilter(implicit
    val mat: Materializer,
    ec: ExecutionContext
) extends Filter
    with Logging {

  import RequestLoggingFilter._

  override def apply(
      next: RequestHeader => Future[Result]
  )(request: RequestHeader): Future[Result] = {
    val wafHeaders = wafHeaderNames.flatMap { name =>
      request.headers.get(name).map(name -> _)
    }

    val start = System.currentTimeMillis()
    val result = next(request)
    result.onComplete { outcome =>
      val status = outcome match {
        case Success(r) => r.header.status.toString
        case Failure(_) => "exception"
      }
      logRequest(
        request,
        wafHeaders,
        status,
        System.currentTimeMillis() - start
      )
    }
    result
  }

  private def logRequest(
      request: RequestHeader,
      wafHeaders: List[(String, String)],
      status: String,
      durationMs: Long
  ): Unit = {
    val clientIp = request.headers
      .get("X-Forwarded-For")
      .flatMap(_.split(",").headOption.map(_.trim))
      .getOrElse(request.remoteAddress)

    val fields = wafHeaders.toMap ++ Map(
      "method" -> request.method,
      "host" -> request.host,
      "path" -> request.path,
      "query" -> request.rawQueryString.take(maxQueryLength),
      "userAgent" -> request.headers.get("User-Agent").getOrElse(""),
      "clientIp" -> clientIp,
      "cloudfrontId" -> request.headers.get("X-Amz-Cf-Id").getOrElse(""),
      "status" -> status,
      "durationMs" -> durationMs.toString,
      "wafMatched" -> wafHeaders.nonEmpty.toString
    )

    val wafSummary =
      if (wafHeaders.isEmpty) ""
      else
        wafHeaders
          .map(_._1.stripPrefix("x-amzn-waf-"))
          .mkString(" WAF match [", ", ", "]")
    log.info(
      Markers.appendEntries(fields.asJava),
      s"${request.method} ${request.path} from $clientIp -> $status (${durationMs}ms)$wafSummary"
    )
  }
}

object RequestLoggingFilter {
  private val maxQueryLength = 500

  val wafHeaderNames: List[String] = List(
    "x-amzn-waf-admin-protection-rule-set",
    "x-amzn-waf-amazon-ip-reputation-list",
    "x-amzn-waf-anonymous-ip-list",
    "x-amzn-waf-common-rule-set",
    "x-amzn-waf-known-bad-inputs-rule-set",
    "x-amzn-waf-linux-rule-set",
    "x-amzn-waf-unix-rule-set"
  )
}
