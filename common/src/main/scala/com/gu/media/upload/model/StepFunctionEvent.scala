package com.gu.media.upload.model

import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format
import com.gu.ai.x.play.json.Jsonx
import com.gu.ai.x.play.json.Encoders._
case class StepFunctionEvent(
    version: String,
    id: String,
    `detail-type`: String,
    source: String,
    account: String,
    time: String,
    region: String,
    resources: List[String],
    detail: StepFunctionDetail
)

object StepFunctionEvent {
  implicit val format: Format[StepFunctionEvent] =
    Jsonx.formatCaseClass[StepFunctionEvent]
}

case class StepFunctionDetail(
    executionArn: String,
    stateMachineArn: String,
    name: String,
    status: String,
    startDate: Long,
    stopDate: Option[Long],
    input: String,
    output: Option[String]
)
object StepFunctionDetail {
  implicit val format: Format[StepFunctionDetail] =
    Jsonx.formatCaseClass[StepFunctionDetail]
}
