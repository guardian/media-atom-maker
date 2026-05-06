package util

import com.gu.media.telemetry.{TagInt, TagLong, TagString, TagValue, Telemetry}
import com.gu.media.upload.model.Upload
import software.amazon.awssdk.services.sfn.model.{
  ExecutionListItem,
  HistoryEvent,
  HistoryEventType,
  ListExecutionsRequest
}

import scala.jdk.CollectionConverters.ListHasAsScala

class Metrics(telemetry: Telemetry, stepFunctions: StepFunctions) {
  val LAMBA_WARM_UP = "LAMBA_WARM_UP"
  def computeDurations(historyEvents: List[HistoryEvent]) = {
    val taskEnteredMap = historyEvents.foldLeft(Map[String, List[Long]]())({
      case (acc, h) =>
        val ts = h.timestamp().toEpochMilli
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
                val previousTimestamps = acc.getOrElse(name, Nil)
                acc.updated(name, ts :: previousTimestamps)
              })
          case HistoryEventType.LAMBDA_FUNCTION_SCHEDULED =>
            val previousTimestamps = acc.getOrElse(LAMBA_WARM_UP, Nil)
            acc.updated(LAMBA_WARM_UP, ts :: previousTimestamps)
          case _ => acc
        }
    })

    val taskExited = historyEvents.foldLeft(Map[String, List[Long]]())({
      case (acc, h) =>
        val ts = h.timestamp().toEpochMilli
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
                val previousTimestamps = acc.getOrElse(name, Nil)
                acc.updated(name, ts :: previousTimestamps)
              })

          case HistoryEventType.LAMBDA_FUNCTION_STARTED =>
            val previousTimestamps = acc.getOrElse(LAMBA_WARM_UP, Nil)
            acc.updated(LAMBA_WARM_UP, ts :: previousTimestamps)
          case _ => acc
        }
    })
    val entered = taskEnteredMap.view.mapValues(ls => ls.sorted)
    val exited = taskExited.view.mapValues(ls => ls.sorted)
    exited.foldLeft(Map[String, Long]())({
      case (acc, (name, exitedDurations)) =>
        val enteredDurations = entered.getOrElse(name, Nil)
        val totalTimes = exitedDurations
          .zip(enteredDurations)
          .map({ case (finish, start) => finish - start })
          .sum
        acc.updated(name, totalTimes)
    })

  }

  def getRunTime(event: ExecutionListItem) = {
    event.stopDate().toEpochMilli - event.startDate().toEpochMilli
  }

  def getStepId(event: ExecutionListItem) = {
    event
      .executionArn()
      .split(":")
      .toList
      .reverse
      .headOption
      .getOrElse(event.executionArn())
  }

  def getMetricsFromEvent(event: ExecutionListItem): Map[String, TagValue] = {
    Map(
      "jobTime" -> TagLong(getRunTime(event)),
      "stepId" -> TagString(getStepId(event))
    )
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
      (s"duration_${k}", TagLong(v))
    })
  }

  def getMetricsFromUpload(upload: Upload) = {
    val size = upload.parts.sortBy(u => -u.end).headOption.fold(0L)(u => u.end)
    Map(
      "chunks" -> TagInt(upload.parts.length),
      "videoSize" -> TagLong(size)
    )
  }

  def run(): Unit = {

    stepFunctions
      .getPreviousExecutions(20)
      .foreach(event => {

        val executionArn = event.executionArn()
        val metricsFromEvent = getMetricsFromEvent(event)
        val historyEvents =
          stepFunctions.getAllHistoryEvents(executionArn)

        val metricsFromHistory =
          getMetricsFromHistory(historyEvents.toList, getRunTime(event))

        val uploadOpt = stepFunctions.getById(getStepId(event))
        val metricsFromUploadData =
          uploadOpt.fold(Map[String, TagValue]())(getMetricsFromUpload)

        telemetry.sendTelemetryEvent(
          "VIDEO_UPLOAD_BACKFILL",
          metricsFromEvent
            ++ metricsFromHistory
            ++ metricsFromUploadData
        )
      })

  }
}
