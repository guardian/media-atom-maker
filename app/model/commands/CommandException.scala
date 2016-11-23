package model.commands

import play.api.mvc.{Results, Result}

case class CommandException(msg: String, responseCode: Int) extends RuntimeException(msg)

object CommandExceptions extends Results {

  def UnknownFailure = throw new CommandException("Unknown internal server error", 500)
  def AtomNotFound = throw new CommandException("Atom not found", 404)
  def AtomIdConflict = throw new CommandException("Atom ID conflict", 400)
  def NotYoutubeAsset = throw new CommandException("Asset is not a youtube video", 400)
  def AssetVersionConflict = throw new CommandException("Asset version conflict", 400)
  def AssetParseFailed = throw new CommandException("Failed to parse asset", 400)
  def AssetEncodingInProcess = throw new CommandException("Asset encoding in process", 400)

  def AtomUpdateFailed(err: String) = throw new CommandException(s"Failed to update atom: $err", 500)
  def AtomPublishFailed(err: String) = throw new CommandException(s"Failed to publish atom: $err", 500)

  // Add exceptions here as required
  def commandExceptionAsResult: PartialFunction[Throwable, Result] = {
    case CommandException(msg, 400) => BadRequest(msg)
    case CommandException(msg, 404) => NotFound(msg)
    case CommandException(msg, 500) => InternalServerError(msg)
  }
}
