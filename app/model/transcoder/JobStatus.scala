package model.transcoder

import ai.x.play.json.Encoders._
import ai.x.play.json.Jsonx

case class JobStatus(key: String, status: String, statusDetail: Option[String])

object JobStatus {
  implicit val jobStatusFormat = Jsonx.formatCaseClass[JobStatus]
}

