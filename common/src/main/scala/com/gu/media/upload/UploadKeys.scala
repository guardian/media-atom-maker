package com.gu.media.upload

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

case class TranscoderOutputKey(year: Int, month: Int, day: Int, id: String, extension: String) {
  override def toString = s"$year/$month/$day/$id.$extension"
}

object TranscoderOutputKey {
  def apply(id: String, extension: String): TranscoderOutputKey = {
    val now = ZonedDateTime.now(ZoneOffset.UTC)
    TranscoderOutputKey(now.getYear, now.getMonthValue, now.getDayOfMonth, id, extension)
  }
}
