package util

import javax.inject.Inject

import akka.stream.Materializer
import play.api.Configuration
import play.api.http.Status
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class RedirectFilter @Inject() (implicit val mat: Materializer,
                                ec: ExecutionContext,
                                val conf: Configuration) extends Filter with Results with Status {

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    conf.getString("host").flatMap(host => {
      if (host != requestHeader.host) {
        Some(Future.successful(Redirect(s"https://$host", MOVED_PERMANENTLY)))
      } else {
        None
      }
    }).getOrElse(nextFilter(requestHeader))
  }
}
