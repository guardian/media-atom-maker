package com.gu.media.util

import java.io.InputStream

import okhttp3.internal.Util
import okhttp3.{MediaType, RequestBody}
import okio.{BufferedSink, Okio, Source}

import scala.annotation.tailrec

// http://stackoverflow.com/questions/25367888/upload-binary-file-with-okhttp-from-resources
// http://stackoverflow.com/questions/25962595/tracking-progress-of-multipart-file-upload-using-okhttp
class InputStreamRequestBody(
    override val contentType: MediaType,
    input: InputStream,
    size: Long
) extends RequestBody {
  private val SEGMENT_SIZE = 2048L // okio.Segment.SIZE

  override def contentLength(): Long = size

  override def writeTo(sink: BufferedSink): Unit = {
    var source: Source = null

    try {
      source = Okio.source(input)
      write(source, sink, 0)
    } finally {
      Util.closeQuietly(source)
    }
  }

  @tailrec
  private def write(
      source: Source,
      sink: BufferedSink,
      uploaded: Long
  ): Unit = {
    size - uploaded match {
      case 0 =>
      // terminate

      case remaining =>
        val amount = if (remaining > SEGMENT_SIZE) { SEGMENT_SIZE }
        else { remaining }
        val written = source.read(sink.buffer(), amount)

        // important otherwise we just make a massive buffer!
        sink.flush()

        if (written != -1) {
          write(source, sink, uploaded + written)
        }
    }
  }
}
