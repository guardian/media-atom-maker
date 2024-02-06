package model.transcoder

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class JobStatus(key: String, status: String, statusDetail: Option[String])

object JobStatus {
  implicit val jobStatusFormat: Format[JobStatus] = Jsonx.formatCaseClass[JobStatus]
}

