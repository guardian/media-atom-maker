package model.commands

import play.api.mvc.{Results, Result}

case class CommandException(msg: String, responseCode: Int) extends RuntimeException(msg)

object CommandExceptions extends Results {

  def UnknownFailure = throw new CommandException("Unknown internal server error", 500)
  def AtomNotFound = throw new CommandException("Atom not found", 404)
  def AtomIdConflict = throw new CommandException("Atom ID conflict", 400)
  def AtomDataStoreError(err: String) = throw new CommandException(err, 500)
  def YouTubeConnectionIssue = throw new CommandException("Could not connect to YouTube", 500)
  def AssetVersionConflict = throw new CommandException("Asset version conflict", 400)
  def AssetParseFailed = throw new CommandException("Failed to parse asset", 400)
  def AssetEncodingInProgress(state: String) = throw CommandException(s"Asset encoding in progress. Current state $state", 400)
  def AssetNotFound = throw new CommandException("Asset not found", 404)

  def AssetNotFound(assetId: String) = throw new CommandException(s"Asset with id $assetId not found", 404)
  def PosterImageUploadFailed(err: String) = throw new CommandException(s"Failed to update poster image (must be at least 1 image asset smaller than 2MB): $err", 400)
  def YoutubeException(err: String) = throw new CommandException(s"Exception when trying to reach YouTube: $err", 400)
  def AtomUpdateFailed(err: String) = throw new CommandException(s"Failed to update atom: $err", 500)
  def AtomPublishFailed(err: String) = throw new CommandException(s"Failed to publish atom: $err", 500)

  def AtomMissingYouTubeChannel = throw new CommandException("Atom is missing YouTube channel", 400)
  def YouTubeVideoDoesNotExist(id: String) = throw new CommandException(s"YouTube video $id does not exist", 400)
  def YouTubeVideoOnIncorrectChannel(expected: String, actual: String) = throw new CommandException(s"Expected YouTube video on channel $expected, got $actual", 400)

  // Add exceptions here as required
  def commandExceptionAsResult: PartialFunction[Throwable, Result] = {
    case CommandException(msg, 400) => BadRequest(msg)
    case CommandException(msg, 404) => NotFound(msg)
    case CommandException(msg, 500) => InternalServerError(msg)
  }
}
