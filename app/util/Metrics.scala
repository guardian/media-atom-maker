package util

import com.gu.media.telemetry.{TagInt, TagLong, TagString, TagValue, Telemetry}
import com.gu.media.upload.model.Upload
import org.joda.time.DateTime
import software.amazon.awssdk.services.sfn.model.{
  ExecutionListItem,
  HistoryEvent,
  HistoryEventType,
  ListExecutionsRequest
}

import java.time.Instant
import scala.jdk.CollectionConverters.ListHasAsScala

class Metrics(telemetry: Telemetry, stepFunctions: StepFunctions) {
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

  def getMetricsFromUpload(upload: Upload): Map[String, TagValue] = {
    val size = upload.parts.sortBy(u => -u.end).headOption.fold(0L)(u => u.end)
    Map(
      "chunks" -> TagInt(upload.parts.length),
      "videoSize" -> TagLong(size)
    )
  }

  def run(): Unit = {
    stepFunctions
      .getPreviousExecutions(500)
      .grouped(20)
      .foreach(events => {
        events.foreach(event => {
          val executionArn = event.executionArn()

          val metricsFromEvent = getMetricsFromEvent(event)
          val historyEvents =
            stepFunctions.getAllHistoryEvents(executionArn)

          val metricsFromHistory =
            getMetricsFromHistory(historyEvents, getRunTime(event))

          val uploadOpt = stepFunctions.getById(getStepId(event))
          val metricsFromUploadData =
            uploadOpt.fold(Map[String, TagValue]())(getMetricsFromUpload)

          telemetry.sendTelemetryEvent(
            "VIDEO_UPLOAD_BACKFILL",
            metricsFromEvent
              ++ metricsFromHistory
              ++ metricsFromUploadData
          )

          Thread.sleep(1000)

        })
        println(s"setting a wait ${DateTime.now}")
        Thread.sleep(5000)
        println(s"finishing the wait ${DateTime.now}")
      })
  }
}
