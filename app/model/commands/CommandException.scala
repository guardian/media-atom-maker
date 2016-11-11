package model.commands

import play.api.mvc.{Results, Result}

case class CommandException(msg: String, responseCode: Int) extends RuntimeException(msg)

object CommandExceptions extends Results {

  def AtomNotFound = throw new CommandException("Atom not found", 404)
  def AtomIdConflict = throw new CommandException("Atom ID conflict", 400)
  def AssetVersionConflict = throw new CommandException("Asset version conflict", 400)
  def AssetParseFailed = throw new CommandException("Failed to parse asset", 400)

  // Add exceptions here as required
  def commandExceptionAsResult: PartialFunction[Throwable, Result] = {
    case CommandException(msg, 400) => BadRequest(msg)
    case CommandException(msg, 404) => NotFound(msg)
  }
}
