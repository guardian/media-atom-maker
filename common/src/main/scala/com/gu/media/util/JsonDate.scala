package com.gu.media.util

import java.time.Duration

import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import org.joda.time.DateTime

object JsonDate {
  implicit object DefaultJodaDateWrites extends Writes[org.joda.time.DateTime] {
    def writes(d: org.joda.time.DateTime): JsValue = JsString(
      ISODateTimeFormat.dateTime.print(d)
    )
  }
  implicit object DefaultJodaDateReads extends Reads[org.joda.time.DateTime] {
    def reads(json: JsValue): JsResult[DateTime] = {
      json match {
        case JsString(dateTime) =>
          JsSuccess(ISODateTimeFormat.dateTime.parseDateTime(dateTime))
        case _ => JsError("DateTime can only be extracted from a JsString")
      }
    }
  }
}

object ISO8601Duration {
  def toSeconds(iso8601Duration: String): Long = {
    Duration.parse(iso8601Duration).toMillis / 1000 // seconds
  }
}
