package util

import com.gu.media.logging.Logging
import net.logstash.logback.marker.Markers
import org.apache.pekko.stream.Materializer
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

class RequestLoggingFilter(implicit val mat: Materializer)
    extends Filter
    with Logging {

  override def apply(
      next: RequestHeader => Future[Result]
  )(request: RequestHeader): Future[Result] = {
    val wafHeaders = RequestLoggingFilter.wafHeaderNames.flatMap { name =>
      request.headers.get(name).map(name -> _)
    }

    if (wafHeaders.nonEmpty) {
      val markers = Markers.appendEntries(
        (wafHeaders.toMap ++ Map(
          "method" -> request.method,
          "path" -> request.path
        )).asJava
      )
      log.info(
        markers,
        s"WAF headers on ${request.method} ${request.path}: ${wafHeaders.map { case (k, v) => s"$k=$v" }.mkString(", ")}"
      )
    }

    next(request)
  }
}

object RequestLoggingFilter {
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
