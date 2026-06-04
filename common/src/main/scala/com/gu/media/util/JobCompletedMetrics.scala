package com.gu.media.util

import com.gu.media.telemetry.{TagInt, TagLong, TagString, TagValue}
import com.gu.media.upload.model.{StepFunctionDetail, StepFunctionEvent, Upload}
import com.gu.media.util.MediaAtomHelpers.extractAtomIdAndVersion
import play.api.libs.json.Json
import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sfn.model.{
  DescribeExecutionRequest,
  DescribeExecutionResponse,
  GetExecutionHistoryRequest,
  HistoryEvent,
  HistoryEventType
}

import scala.jdk.CollectionConverters.IterableHasAsScala

class JobCompletedMetrics(stepFunctionsClient: SfnClient) {
  val LAMBA_WARM_UP = "LAMBA_WARM_UP"
  val UNKNOWN_JOB = "UNKNOWN_JOB"
  def computeDurations(historyEvents: List[HistoryEvent]) = {
    val taskEnteredMap =
      historyEvents.foldLeft(Map[String, List[HistoryEvent]]())({
        case (acc, h) =>
          h.`type`() match {
            case HistoryEventType.TASK_STATE_ENTERED |
                HistoryEventType.WAIT_STATE_ENTERED |
                HistoryEventType.PASS_STATE_ENTERED |
                HistoryEventType.CHOICE_STATE_ENTERED |
                HistoryEventType.PARALLEL_STATE_ENTERED |
                HistoryEventType.MAP_STATE_ENTERED =>
              Option(h.stateEnteredEventDetails())
                .map(_.name())
                .fold(acc)(name => {
                  acc.updated(name, h :: acc.getOrElse(name, Nil))
                })
            case HistoryEventType.LAMBDA_FUNCTION_SCHEDULED =>
              acc.updated(LAMBA_WARM_UP, h :: acc.getOrElse(LAMBA_WARM_UP, Nil))
            case _ => acc
          }
      })

    val taskExited = historyEvents.foldLeft(Map[String, List[HistoryEvent]]())({
      case (acc, h) =>
        h.`type`() match {
          case HistoryEventType.TASK_STATE_EXITED |
              HistoryEventType.WAIT_STATE_EXITED |
              HistoryEventType.PASS_STATE_EXITED |
              HistoryEventType.CHOICE_STATE_EXITED |
              HistoryEventType.PARALLEL_STATE_EXITED |
              HistoryEventType.MAP_STATE_EXITED =>
            Option(h.stateExitedEventDetails())
              .map(_.name())
              .fold(acc)(name => {
                acc.updated(name, h :: acc.getOrElse(name, Nil))
              })

          case HistoryEventType.LAMBDA_FUNCTION_STARTED =>
            acc.updated(LAMBA_WARM_UP, h :: acc.getOrElse(LAMBA_WARM_UP, Nil))
          case _ => acc
        }
    })
    val durations = taskEnteredMap.foldLeft(Map[String, Long]())({
      case (durationsAcc, (id, events)) =>
        val enteredEvents = events.sortBy(_.id())
        val exitedEvents = taskExited.getOrElse(id, Nil).sortBy(_.id())

        val durations = for {
          (enteredX, exitedX) <- enteredEvents.zip(exitedEvents)
          if enteredX.id() < exitedX.id()
        } yield {
          exitedX.timestamp().toEpochMilli - enteredX.timestamp().toEpochMilli
        }

        val name = enteredEvents.headOption
          .flatMap(h =>
            h.`type`() match {
              case HistoryEventType.LAMBDA_FUNCTION_STARTED =>
                Some(LAMBA_WARM_UP)
              case _ => Option(h.stateEnteredEventDetails()).map(_.name())
            }
          )
          .getOrElse(UNKNOWN_JOB)

        durationsAcc.updated(name, durations.sum)
    })

    durations

  }

  def getRunTime(stepFunctionDetail: StepFunctionDetail) = {
    stepFunctionDetail.stopDate
      .map(d => d - stepFunctionDetail.startDate)
      .getOrElse(0L)
  }

  def getMetricsFromHistory(
      historyEvents: List[HistoryEvent],
      runTime: Long
  ): Map[String, TagValue] = {
    val durationsMap = computeDurations(historyEvents)
    val totalSubTimes = durationsMap.foldLeft(0L)({ case (acc, (_, v)) =>
      acc + v
    })
    Map(
      "totalSubTimes" -> TagLong(totalSubTimes),
      "duration_unknown" -> TagLong(runTime - totalSubTimes)
    ) ++ durationsMap.map({ case (k, v) =>
      (s"duration_$k", TagLong(v))
    })
  }

  def getMetricsFromUpload(upload: Upload): Map[String, TagValue] = {
    val size = upload.parts.sortBy(u => -u.end).headOption.fold(0L)(u => u.end)
    Map(
      "chunks" -> TagInt(upload.parts.length),
      "videoSize" -> TagLong(size)
    )
  }
  def getAllHistoryEvents(executionArn: String) = {
    def getAllHistory(
        nextToken: Option[String],
        previousEvents: List[HistoryEvent]
    ): List[HistoryEvent] = {
      val request = GetExecutionHistoryRequest
        .builder()
        .executionArn(executionArn)
        .nextToken(nextToken.orNull)
        .build()

      val response = stepFunctionsClient.getExecutionHistory(request)
      val allEvents = response.events().asScala.toList ::: previousEvents
      if (response.nextToken() != null) {
        getAllHistory(Some(response.nextToken()), allEvents)
      } else allEvents
    }
    getAllHistory(None, Nil)
  }

  def getMetricsForJobRun(event: StepFunctionEvent) = {
    val runTime = getRunTime(event.detail)
    val historyEvents = getAllHistoryEvents(event.detail.executionArn)
    val metricsFromHistory = getMetricsFromHistory(historyEvents, runTime)
    val uploadOpt = Json.parse(event.detail.input).validate[Upload].asOpt
    val metricsFromUploadData =
      uploadOpt.fold(Map[String, TagValue]())(getMetricsFromUpload)
    val (atomId, version) = extractAtomIdAndVersion(event.id)

    Map(
      "jobTime" -> TagLong(runTime),
      "stepFunctionId" -> TagString(event.id),
      "atomId" -> TagString(atomId),
      "version" -> TagString(version)
    ) ++ metricsFromHistory ++ metricsFromUploadData
  }
}
