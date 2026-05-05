package util

import com.gu.media.telemetry.Telemetry
import software.amazon.awssdk.services.sfn.model.{
  HistoryEvent,
  HistoryEventType,
  ListExecutionsRequest
}

import scala.jdk.CollectionConverters.ListHasAsScala

class Metrics(telemetry: Telemetry, stepFunctions: StepFunctions) {

  def computeDurations(historyEvents: List[HistoryEvent]) = {
    val taskEnteredMap = historyEvents.foldLeft(Map[String, Long]())({
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
                acc.updated(name, ts)
              })
          case HistoryEventType.LAMBDA_FUNCTION_SCHEDULED =>
            acc.updated("LAMBA_WARM_UP", ts)
          case _ => acc
        }
    })

    historyEvents.foldLeft(Map[String, Long]())({ case (acc, h) =>
      val ts = h.timestamp().toEpochMilli
      h.`type`() match {
        case HistoryEventType.TASK_STATE_EXITED |
            HistoryEventType.WAIT_STATE_EXITED |
            HistoryEventType.PASS_STATE_EXITED |
            HistoryEventType.CHOICE_STATE_EXITED |
            HistoryEventType.PARALLEL_STATE_EXITED |
            HistoryEventType.MAP_STATE_EXITED =>
          (for {
            eventDetails <- Option(h.stateExitedEventDetails())
            name = eventDetails.name()
            start <- taskEnteredMap.get(name)
            duration = ts - start
          } yield {
            acc.updated(name, duration)
          }).getOrElse(acc)

        case HistoryEventType.LAMBDA_FUNCTION_STARTED => {
          taskEnteredMap
            .get("LAMBA_WARM_UP")
            .fold(acc)(start => {
              acc.updated("LAMBA_WARM_UP", ts - start)
            })
        }
        case _ => acc
      }
    })
  }

  def run(): Unit = {

    val executions = stepFunctions.getPreviousExecutions(1)
    val historyEvents =
      stepFunctions.getEventsInReverseOrder(executions.headOption.get)
    val durationsMap = computeDurations(historyEvents.toList)

    telemetry.sendTelemetryEvent(
      "test",
      Map("id" -> 4L) ++ durationsMap.map({ case (k, v) =>
        (s"duration_${k}", v)
      })
    )
    println("Running the metrics class")
  }
}
