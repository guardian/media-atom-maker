package util

import com.gu.pandomainauth.action.UserRequest
import play.api.mvc.AnyContent

import scala.util.Try

trait TrainingMode {
  def isInTrainingMode(req: UserRequest[AnyContent]): Boolean = {
    req.session.get("isTrainingMode") match {
      case Some(trainingMode) => Try(trainingMode.toBoolean).getOrElse(false)
      case None               => false
    }
  }
}
