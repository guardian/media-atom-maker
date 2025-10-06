package model.commands

import com.google.api.client.googleapis.json.{GoogleJsonError, GoogleJsonResponseException}
import com.google.api.client.http.{HttpHeaders, HttpResponseException}
import com.gu.media.logging.Logging
import play.api.mvc.{Result, Results}
import scala.jdk.CollectionConverters._

case class CommandException(msg: String, responseCode: Int) extends RuntimeException(msg)

object CommandExceptions extends Results {

  def UnknownFailure = throw new CommandException("Unknown internal server error", 500)
  def AtomNotFound = throw new CommandException("Atom not found", 404)
  def AtomIdConflict = throw new CommandException("Atom ID conflict", 400)
  def AtomDataStoreError(err: String) = throw new CommandException(err, 500)
  def YouTubeConnectionIssue = throw new CommandException("Could not connect to YouTube", 500)
  def NotYoutubeAsset = throw new CommandException("Asset is not a youtube video", 400)
  def AssetVersionConflict = throw new CommandException("Asset version conflict", 400)
  def AssetAlreadyAdded = throw new CommandException("Asset has already been added to this atom", 400)
  def AssetParseFailed = throw new CommandException("Failed to parse asset", 400)
  def AssetNotFound = throw new CommandException("Asset not found", 404)
  def CannotDeleteActiveAsset = throw new CommandException("Cannot delete active asset", 400)

  def AssetNotFound(assetId: String) = throw new CommandException(s"Asset with id $assetId not found", 404)
  def YoutubeException(err: String) = throw new CommandException(s"Exception when trying to reach YouTube: $err", 400)
  def AtomUpdateFailed(err: String) = throw new CommandException(s"Failed to update atom: $err", 500)
  def AtomUpdateConflictError(err: String) = throw new CommandException(s"Failed to update atom: $err", 409)
  def AtomPublishFailed(err: String) = throw new CommandException(s"Failed to publish atom: $err", 500)

  def AtomMissingYouTubeChannel = throw new CommandException("Atom is missing YouTube channel", 400)
  def YouTubeVideoDoesNotExist(id: String) = throw new CommandException(s"YouTube video $id does not exist", 400)
  def IncorrectYouTubeChannel = throw new CommandException(s"New video is not on the same YouTube channel", 400)
  def NotGLabsAtom = throw new CommandException(s"Third party videos can only be added to GLabs Atoms", 400)

  def SubtitleFileUploadFailed = throw new CommandException("Failed to upload subtitle file", 500)

  // Add exceptions here as required
  def commandExceptionAsResult: PartialFunction[Throwable, Result] = {
    case CommandException(msg, 400) => BadRequest(msg)
    case CommandException(msg, 404) => NotFound(msg)
    case CommandException(msg, 409) => Conflict(msg)
    case CommandException(msg, 500) => InternalServerError(msg)
    case YouTubeError(msg, true) => InternalServerError(msg)
    case YouTubeError(msg, false) => InternalServerError(msg).withHeaders("X-No-Alerts" -> "true")
  }
}

object YouTubeError extends Logging {
  def unapply(err: Throwable): Option[(String, Boolean)] = err match {
    case e: GoogleJsonResponseException =>
      (e.getDetails.getCode, getDomain(e)) match {
        case (503, Some("global")) =>
          Some((noAlerts(e), false))

        case (403, Some("usageLimits")) =>
          Some((noAlerts(e), false))

        case (400, Some("youtubePartner.videoAdvertisingOptions.get")) => {
          Some((noAlerts(e), false))
        }

        case (code, _) =>
          val message = Option(e.getDetails.getMessage).getOrElse("unknown")

          log.warn(s"YouTube failure. Code: $code. Message: $message")
          Some((s"YouTube $code: $message", true)) // alerts
      }

    case _ =>
      None
  }

  private def getDomain(e: GoogleJsonResponseException): Option[String] = {
    val errors = e.getDetails.getErrors.asScala
    errors.headOption.flatMap { err => Some(err.getDomain) }
  }

  // For testing
  val rateLimitExceeded: GoogleJsonResponseException =
    buildYouTubeException(403, "usageLimits", "User Rate Limit Exceeded", "userRateLimitExceeded")

  val backendError: GoogleJsonResponseException =
    buildYouTubeException(503, "global", "Backend Error", "backendError")

  private def buildYouTubeException(code: Int, domain: String, message: String, reason: String): GoogleJsonResponseException = {
    val info = new GoogleJsonError.ErrorInfo()
    info.setDomain(domain)
    info.setMessage(message)
    info.setReason(reason)

    val error = new GoogleJsonError()
    error.setCode(code)
    error.setErrors(List(info).asJava)
    error.setMessage(message)

    val builder = new HttpResponseException.Builder(503, null, new HttpHeaders())
    builder.setMessage(message)

    new GoogleJsonResponseException(builder, error)
  }

  private def noAlerts(e: GoogleJsonResponseException): String = {
    val msg = s"YouTube ${e.getDetails.getCode} ${e.getDetails.getMessage}"
    log.warn(msg, e) // to get stack trace in the logs

    msg
  }
}
