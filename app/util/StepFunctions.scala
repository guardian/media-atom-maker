package util

import java.time.Instant

import com.amazonaws.services.stepfunctions.model._
import com.fasterxml.jackson.core.JsonParseException
import com.gu.media.upload.model._
import play.api.libs.json.{JsResultException, JsSuccess, Json}

import scala.collection.JavaConverters._

class StepFunctions(awsConfig: AWSConfig) {
  def getById(id: String): Option[Upload] = {
    val arn = s"${awsConfig.pipelineArn.replace(":stateMachine:", ":execution:")}:$id"

    try {
      val request = new DescribeExecutionRequest().withExecutionArn(arn)
      val result = awsConfig.stepFunctionsClient.describeExecution(request)
      val json = Json.parse(result.getInput)

      Some(json.as[Upload])
    } catch {
      case _: ExecutionDoesNotExistException =>
        None
    }
  }

  def getStatus(atomId: String): Iterable[UploadStatus] = {
    val runningJobs = getExecutions(atomId, ExecutionStatus.RUNNING)
    val failedJobs = getExecutions(atomId, ExecutionStatus.FAILED).filter(lessThan10MinutesOld)

    val running = runningJobs.map(getRunningStatus)
    val failed = failedJobs.map(getFailedStatus)

    running ++ failed
  }

  def start(upload: Upload): Unit = {
    val stepFunctionsRequest = new StartExecutionRequest()
      .withName(upload.id)
      .withStateMachineArn(awsConfig.pipelineArn)
      .withInput(Json.stringify(Json.toJson(upload)))

    awsConfig.stepFunctionsClient.startExecution(stepFunctionsRequest)
  }

  private def getExecutions(atomId: String, filter: ExecutionStatus): Iterable[ExecutionListItem] = {
    val request = new ListExecutionsRequest()
      .withStateMachineArn(awsConfig.pipelineArn)
      .withStatusFilter(filter)

    val results = awsConfig.stepFunctionsClient.listExecutions(request).getExecutions.asScala

    results.filter(_.getName.startsWith(atomId))
  }

  private def getEvents(execution: ExecutionListItem): Iterable[HistoryEvent] = {
    val request = new GetExecutionHistoryRequest()
      .withExecutionArn(execution.getExecutionArn)
      .withReverseOrder(true)
      .withMaxResults(20)

    awsConfig.stepFunctionsClient.getExecutionHistory(request).getEvents.asScala
  }

  private def getRunningStatus(execution: ExecutionListItem): UploadStatus = {
    val id = execution.getName

    getLastTask(execution) match {
      case Some((state, upload)) =>
        buildProgress(id, state, upload)

      case None =>
        UploadStatus.indeterminate(id, "Uploading")
    }
  }

  private def getFailedStatus(execution: ExecutionListItem): UploadStatus = {
    val id = execution.getName
    val events = getEvents(execution)

    val cause = events.find(_.getType == "ExecutionFailed") match {
      case Some(event) =>
        val cause = event.getExecutionFailedEventDetails.getCause

        try {
          (Json.parse(cause) \ "errorMessage").as[String]
        } catch {
          case _: JsonParseException | _: JsResultException =>
            id
        }

      case None => "Failed (unknown error)"
    }

    UploadStatus.indeterminate(id, cause, failed = true)
  }

  private def getLastTask(execution: ExecutionListItem): Option[(String, Upload)] = {
    val events = getEvents(execution)
    val taskEvent = events.find(_.getType == "TaskStateEntered")

    taskEvent.map { event =>
      val state = event.getStateEnteredEventDetails.getName
      val upload = Json.parse(event.getStateEnteredEventDetails.getInput).as[Upload]

      (state, upload)
    }
  }

  private def lessThan10MinutesOld(e: ExecutionListItem): Boolean = {
    val now = Instant.now().toEpochMilli
    val end = e.getStopDate.toInstant.toEpochMilli

    (now - end) < (1000 * 60 * 10)
  }

  private def buildProgress(id: String, state: String, upload: Upload): UploadStatus = {
    if(upload.metadata.selfHost) {
      UploadStatus.indeterminate(id, state)
    } else {
      val current = upload.progress.chunksInYouTube
      val total = upload.parts.length

      val status = if(current < total) {
        UploadStatus(id, "Uploading to YouTube", current, total)
      } else {
        UploadStatus.indeterminate(id, state)
      }

      status.copy(assetAdded = upload.metadata.youTubeId.nonEmpty)
    }
  }
}
