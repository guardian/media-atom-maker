package com.gu.media.upload

import java.time.format.DateTimeFormatter
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

case class TranscoderOutputKey(prefix: String, title: String, id: String, extension: String) {
  override def toString = s"$prefix/$title--$id.$extension"
}

object TranscoderOutputKey {
  def apply(title: String, id: String, extension: String): TranscoderOutputKey = {
    val now = ZonedDateTime.now(ZoneOffset.UTC)
    val prefix = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    TranscoderOutputKey(prefix, title, id, extension)
  }
}
