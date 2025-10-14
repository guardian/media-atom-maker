package model.transcoder

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.OFormat

case class JobStatus(key: String, status: String, statusDetail: Option[String])

object JobStatus {
  implicit val jobStatusFormat: OFormat[JobStatus] =
    Jsonx.formatCaseClass[JobStatus]
}
