package util

import com.gu.media.telemetry.{TagNumber, TagString, TagValue, Telemetry}
import software.amazon.awssdk.services.sfn.model.{
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

  def getStepIdFromArn(arn: String) = {
    arn.split(":").toList.reverse.headOption
  }

  def executionArn(stepId: String) = {
    s"arn:aws:states:eu-west-1:563563610310:execution:VideoPipelineDEV-PGZ5E0CNI0QG:$stepId"
  }

  def generateMetrics(
      executionArn: String,
      runTime: Long
  ): Map[String, TagValue] = {
    val historyEvents =
      stepFunctions.getAllHistoryEvents(executionArn)

    val durationsMap = computeDurations(historyEvents.toList)
    val totalSubTimes = durationsMap.foldLeft(0L)({ case (acc, (_, v)) =>
      acc + v
    })
    val stepId = getStepIdFromArn(executionArn).getOrElse(executionArn)
    Map(
      "stepId" -> TagString(stepId),
      "jobTime" -> TagNumber(runTime),
      "totalSubTimes" -> TagNumber(totalSubTimes),
      "duration_unknown" -> TagNumber(runTime - totalSubTimes)
    ) ++ durationsMap.map({ case (k, v) =>
      (s"duration_${k}", TagNumber(v))
    })
  }

  def run(): Unit = {

    stepFunctions
      .getPreviousExecutions(20)
      .foreach(event => {
        val arn = event.executionArn()

        val runTime =
          event.stopDate().toEpochMilli - event.startDate().toEpochMilli

        val tags = generateMetrics(arn, runTime)

        telemetry.sendTelemetryEvent(
          "test",
          tags
        )
      })

  }
}
