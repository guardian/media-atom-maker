package util

import java.time.Instant

import com.amazonaws.services.stepfunctions.model._
import com.fasterxml.jackson.core.JsonParseException
import com.gu.media.upload.model._
import play.api.libs.json.{JsResultException, Json}

import scala.jdk.CollectionConverters._

class StepFunctions(awsConfig: AWSConfig) {
  def getById(id: String): Option[Upload] = {
    val arn =
      s"${awsConfig.pipelineArn.replace(":stateMachine:", ":execution:")}:$id"

    try {
      val request = new DescribeExecutionRequest().withExecutionArn(arn)
      val result = awsConfig.stepFunctionsClient.describeExecution(request)

      val upload = Json.parse(result.getInput).validate[Upload].asOpt
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
      event <- events.find(_.getType == "TaskStateEntered")

      details = event.getStateEnteredEventDetails
      upload <- Json.parse(details.getInput).validate[Upload].asOpt
    } yield {
      details.getName -> upload
    }

  def getExecutionFailed(events: Iterable[HistoryEvent]): Option[String] = {
    events.find(_.getType == "ExecutionFailed").flatMap { event =>
      val cause = event.getExecutionFailedEventDetails.getCause

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
    val stepFunctionsRequest = new StartExecutionRequest()
      .withName(executionName)
      .withStateMachineArn(awsConfig.pipelineArn)
      .withInput(Json.stringify(Json.toJson(upload)))

    awsConfig.stepFunctionsClient.startExecution(stepFunctionsRequest)
  }

  def getEventsInReverseOrder(
      execution: ExecutionListItem
  ): Iterable[HistoryEvent] = {
    val request = new GetExecutionHistoryRequest()
      .withExecutionArn(execution.getExecutionArn)
      .withReverseOrder(true)
      .withMaxResults(20)

    awsConfig.stepFunctionsClient.getExecutionHistory(request).getEvents.asScala
  }

  private def getExecutions(
      atomId: String,
      filter: ExecutionStatus
  ): Iterable[ExecutionListItem] = {
    val request = new ListExecutionsRequest()
      .withStateMachineArn(awsConfig.pipelineArn)
      .withStatusFilter(filter)

    val results = awsConfig.stepFunctionsClient
      .listExecutions(request)
      .getExecutions
      .asScala

    results.filter(_.getName.startsWith(atomId))
  }

  private def lessThan10MinutesOld(e: ExecutionListItem): Boolean = {
    val now = Instant.now().toEpochMilli
    val end = e.getStopDate.toInstant.toEpochMilli

    (now - end) < (1000 * 60 * 10)
  }

  private def fillInStartTimestamp(
      result: DescribeExecutionResult,
      upload: Upload
  ): Upload = {
    if (upload.metadata.startTimestamp.isEmpty) {
      upload.copy(
        metadata = upload.metadata.copy(
          startTimestamp = Some(result.getStartDate.getTime)
        )
      )
    } else {
      upload
    }
  }
}
