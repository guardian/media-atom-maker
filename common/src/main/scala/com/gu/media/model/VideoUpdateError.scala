package com.gu.media.model

case class VideoUpdateError(
    errorToLog: String,
    errorToClient: Option[String] = None
) {
  def getErrorToClient(): String = {
    errorToClient match {
      case Some(error) => error
      case None        => errorToLog
    }
  }
}
