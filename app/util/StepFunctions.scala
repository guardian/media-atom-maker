package util

import java.time.Instant

import com.amazonaws.services.stepfunctions.model._
import com.fasterxml.jackson.core.JsonParseException
import com.gu.media.upload.model._
import play.api.libs.json.{JsResultException, Json}

import scala.collection.JavaConverters._

class StepFunctions(awsConfig: AWSConfig) {
  def getById(id: String): Option[(Long, Upload)] = {
    val arn = s"${awsConfig.pipelineArn.replace(":stateMachine:", ":execution:")}:$id"

    try {
      val request = new DescribeExecutionRequest().withExecutionArn(arn)
      val result = awsConfig.stepFunctionsClient.describeExecution(request)

      val json = Json.parse(result.getInput)

      Some(result.getStartDate.getTime, json.as[Upload])
    } catch {
      case _: ExecutionDoesNotExistException =>
        None
    }
  }

  def getJobs(atomId: String): Iterable[ExecutionListItem] = {
    val runningJobs = getExecutions(atomId, ExecutionStatus.RUNNING)
    val failedJobs = getExecutions(atomId, ExecutionStatus.FAILED).filter(lessThan10MinutesOld)

    runningJobs ++ failedJobs
  }

  def getTaskEntered(events: Iterable[HistoryEvent]): Option[(String, Upload)] = {
    events.find(_.getType == "TaskStateEntered").map { event =>
      val details = event.getStateEnteredEventDetails
      val upload = Json.parse(details.getInput).as[Upload]

      (details.getName, upload)
    }
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

  def start(upload: Upload): Unit = {
    val stepFunctionsRequest = new StartExecutionRequest()
      .withName(upload.id)
      .withStateMachineArn(awsConfig.pipelineArn)
      .withInput(Json.stringify(Json.toJson(upload)))

    awsConfig.stepFunctionsClient.startExecution(stepFunctionsRequest)
  }

  def getEventsInReverseOrder(execution: ExecutionListItem): Iterable[HistoryEvent] = {
    val request = new GetExecutionHistoryRequest()
      .withExecutionArn(execution.getExecutionArn)
      .withReverseOrder(true)
      .withMaxResults(20)

    awsConfig.stepFunctionsClient.getExecutionHistory(request).getEvents.asScala
  }

  private def getExecutions(atomId: String, filter: ExecutionStatus): Iterable[ExecutionListItem] = {
    val request = new ListExecutionsRequest()
      .withStateMachineArn(awsConfig.pipelineArn)
      .withStatusFilter(filter)

    val results = awsConfig.stepFunctionsClient.listExecutions(request).getExecutions.asScala

    results.filter(_.getName.startsWith(atomId))
  }

  private def lessThan10MinutesOld(e: ExecutionListItem): Boolean = {
    val now = Instant.now().toEpochMilli
    val end = e.getStopDate.toInstant.toEpochMilli

    (now - end) < (1000 * 60 * 10)
  }
}
