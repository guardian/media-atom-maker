package com.gu.media.util

import java.io.InputStream

import com.squareup.okhttp.internal.Util
import com.squareup.okhttp.{MediaType, RequestBody}
import okio.{BufferedSink, Okio, Source}

import scala.annotation.tailrec

// http://stackoverflow.com/questions/25367888/upload-binary-file-with-okhttp-from-resources
// http://stackoverflow.com/questions/25962595/tracking-progress-of-multipart-file-upload-using-okhttp
class InputStreamRequestBody(override val contentType: MediaType, input: InputStream, size: Long, progress: Long => Unit) extends RequestBody {
  private val SEGMENT_SIZE = 2048L // okio.Segment.SIZE
  private val LOGGING_THROTTLE = 10000

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
  private def write(source: Source, sink: BufferedSink, uploaded: Long): Unit = {
    if(uploaded % LOGGING_THROTTLE == 0)
      progress(uploaded)

    size - uploaded match {
      case 0 =>
      // terminate

      case remaining =>
        val amount = if(remaining > SEGMENT_SIZE) { SEGMENT_SIZE } else { remaining }
        val written = source.read(sink.buffer(), amount)

        // important otherwise we just make a massive buffer!
        sink.flush()

        if(written != -1) {
          write(source, sink, uploaded + written)
        }
    }
  }
}
