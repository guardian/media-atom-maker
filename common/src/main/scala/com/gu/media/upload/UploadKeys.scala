package com.gu.media.upload

import java.net.URLEncoder
import java.time.{ZoneOffset, ZonedDateTime}

case class UploadKey(folder: String, id: String) {
  override def toString = s"$folder/$id"
}

case class UploadPartKey(folder: String, id: String, part: Int) {
  override def toString = s"$folder/$id/parts/$part"
}

case class CompleteUploadKey(folder: String, id: String) {
  override def toString = s"$folder/$id/complete"
}

case class TranscoderOutputKey(year: Int, month: Int, day: Int, title: String, id: String, extension: String) {
  override def toString = s"$year/$month/$day/${URLEncoder.encode(title, "UTF-8")}-$id.$extension"
}

object TranscoderOutputKey {
  def apply(title: String, id: String, extension: String): TranscoderOutputKey = {
    val now = ZonedDateTime.now(ZoneOffset.UTC)
    TranscoderOutputKey(now.getYear, now.getMonthValue, now.getDayOfMonth, title, id, extension)
  }
}
