package model.commands

import com.google.api.client.googleapis.json.{GoogleJsonError, GoogleJsonResponseException}
import play.api.mvc.{Result, Results}

case class CommandException(msg: String, responseCode: Int) extends RuntimeException(msg)

object CommandExceptions extends Results {

  def UnknownFailure = throw new CommandException("Unknown internal server error", 500)
  def AtomNotFound = throw new CommandException("Atom not found", 404)
  def AtomIdConflict = throw new CommandException("Atom ID conflict", 400)
  def AtomDataStoreError(err: String) = throw new CommandException(err, 500)
  def YouTubeConnectionIssue = throw new CommandException("Could not connect to YouTube", 500)
  def NotYoutubeAsset = throw new CommandException("Asset is not a youtube video", 400)
  def AssetVersionConflict = throw new CommandException("Asset version conflict", 400)
  def AssetParseFailed = throw new CommandException("Failed to parse asset", 400)
  def AssetNotFound = throw new CommandException("Asset not found", 404)

  def AssetNotFound(assetId: String) = throw new CommandException(s"Asset with id $assetId not found", 404)
  def PosterImageUploadFailed(err: String) = throw new CommandException(s"Failed to update poster image (must be at least 1 image asset smaller than 2MB): $err", 400)
  def YoutubeException(err: String) = throw new CommandException(s"Exception when trying to reach YouTube: $err", 400)
  def AtomUpdateFailed(err: String) = throw new CommandException(s"Failed to update atom: $err", 500)
  def AtomPublishFailed(err: String) = throw new CommandException(s"Failed to publish atom: $err", 500)

  def AtomMissingYouTubeChannel = throw new CommandException("Atom is missing YouTube channel", 400)
  def YouTubeVideoDoesNotExist(id: String) = throw new CommandException(s"YouTube video $id does not exist", 400)
  def IncorrectYouTubeChannel = throw new CommandException(s"New video is not on the same YouTube channel", 400)

  // Add exceptions here as required
  def commandExceptionAsResult: PartialFunction[Throwable, Result] = {
    case CommandException(msg, 400) => BadRequest(msg)
    case CommandException(msg, 404) => NotFound(msg)
    case CommandException(msg, 500) => InternalServerError(msg)
    case YouTubeBackendError(err) => ServiceUnavailable(s"YouTube backend error: ${err.getMessage}")
  }
}

object YouTubeBackendError {
  def unapply(err: Throwable): Option[GoogleJsonError] = err match {
    case e: GoogleJsonResponseException =>
      val code = e.getDetails.getCode

      if(code >= 500 && code < 600) {
        Some(e.getDetails)
      } else {
        None
      }

    case _ =>
      None
  }
}
