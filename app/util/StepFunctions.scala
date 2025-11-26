package util

import java.time.Instant

import software.amazon.awssdk.services.sfn.model._
import com.fasterxml.jackson.core.JsonParseException
import com.gu.media.upload.model._
import play.api.libs.json.{JsResultException, Json}

import scala.jdk.CollectionConverters._

class StepFunctions(awsConfig: AWSConfig) {
  def getById(id: String): Option[Upload] = {
    val arn =
      s"${awsConfig.pipelineArn.replace(":stateMachine:", ":execution:")}:$id"
    try {
      val request = DescribeExecutionRequest.builder().executionArn(arn).build()
      val result = awsConfig.stepFunctionsClient.describeExecution(request)

      val upload = Json.parse(result.input()).validate[Upload].asOpt
      upload.map(fillInStartTimestamp(result, _))
    } catch {
      case _: ExecutionDoesNotExistException =>
        None
    }
  }

  def getJobs(atomId: String): Iterable[ExecutionListItem] = {
    val runningJobs = getExecutions(atomId, ExecutionStatus.RUNNING)
    val failedJobs =
      getExecutions(atomId, ExecutionStatus.FAILED).filter(lessThan10MinutesOld)
    runningJobs ++ failedJobs
  }

  def getTaskEntered(events: Iterable[HistoryEvent]): Option[(String, Upload)] =
    for {
      event <- events.find(_.`type`() == "TaskStateEntered")
      details = event.stateEnteredEventDetails
      upload <- Json.parse(details.input).validate[Upload].asOpt
    } yield {
      details.name -> upload
    }

  def getExecutionFailed(events: Iterable[HistoryEvent]): Option[String] = {
    events.find(_.`type`() == "ExecutionFailed").flatMap { event =>
      val cause = event.executionFailedEventDetails.cause()
      try {
        Some((Json.parse(cause) \ "errorMessage").as[String])
      } catch {
        case _: JsonParseException | _: JsResultException =>
          Some(cause)
      }
    }
  }

  def start(upload: Upload, withCustomName: Option[String] = None): Unit = {
    val executionName = withCustomName.getOrElse(upload.id)
    val stepFunctionsRequest = StartExecutionRequest.builder()
      .name(executionName)
      .stateMachineArn(awsConfig.pipelineArn)
      .input(Json.stringify(Json.toJson(upload)))
      .build()

    awsConfig.stepFunctionsClient.startExecution(stepFunctionsRequest)
  }

  def getEventsInReverseOrder(
      execution: ExecutionListItem
  ): Iterable[HistoryEvent] = {
    val request = GetExecutionHistoryRequest.builder()
      .executionArn(execution.executionArn)
      .reverseOrder(true)
      .maxResults(20)
      .build()

    awsConfig.stepFunctionsClient.getExecutionHistory(request).events().asScala
  }

  private def getExecutions(
      atomId: String,
      filter: ExecutionStatus
  ): Iterable[ExecutionListItem] = {
    val request = ListExecutionsRequest.builder()
      .stateMachineArn(awsConfig.pipelineArn)
      .statusFilter(filter)
      .build()

    val results = awsConfig.stepFunctionsClient
      .listExecutions(request)
      .executions()
      .asScala

    results.filter(_.name.startsWith(atomId))
  }

  private def lessThan10MinutesOld(e: ExecutionListItem): Boolean = {
    val now = Instant.now().toEpochMilli
    val end = e.stopDate.toEpochMilli

    (now - end) < (1000 * 60 * 10)
  }

  private def fillInStartTimestamp(
      result: DescribeExecutionResponse,
      upload: Upload
  ): Upload = {
    if (upload.metadata.startTimestamp.isEmpty) {
      upload.copy(
        metadata = upload.metadata.copy(
          // TODO -> check if this is the equivalent of calling getTime
          startTimestamp = Some(result.startDate.toEpochMilli)
        )
      )
    } else {
      upload
    }
  }
}
