package model.transcoder

import org.cvogt.play.json.Jsonx

case class JobStatus(key: String, status: String, statusDetail: Option[String])

object JobStatus {
  implicit val jobStatusFormat = Jsonx.formatCaseClass[JobStatus]
}

