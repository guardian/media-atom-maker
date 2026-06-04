package com.gu.media.util

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import software.amazon.awssdk.services.sfn.model.{
  HistoryEvent,
  HistoryEventType
}

import java.time.Instant

class JobCompletedMetricsTest extends AnyFunSuite with Matchers {

  val metrics = new JobCompletedMetrics(null)

  def event(
      id: Long,
      eventType: HistoryEventType,
      epochMilli: Long
  ): HistoryEvent =
    HistoryEvent
      .builder()
      .id(id)
      .`type`(eventType)
      .timestamp(Instant.ofEpochMilli(epochMilli))
      .build()

  test("computeDurations calculates should use a custom name for the lambda warm up time") {
    val scheduled = event(1L, HistoryEventType.LAMBDA_FUNCTION_SCHEDULED, 1000L)
    val started = event(2L, HistoryEventType.LAMBDA_FUNCTION_STARTED, 1500L)

    val result = metrics.computeDurations(List(scheduled, started))

    result must contain(metrics.LAMBA_WARM_UP -> 500L)
  }
}
