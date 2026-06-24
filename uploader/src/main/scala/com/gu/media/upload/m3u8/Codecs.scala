package com.gu.media.upload.m3u8

import java.io.InputStream

object Codecs {
  def extractCodecs(line: String): Set[String] = {
    val codecsRegex = """#EXT-X-STREAM-INF:.*CODECS="([^"]+)"""".r
    codecsRegex.findFirstMatchIn(line) match {
      case Some(m) =>
        m.group(1).split(",").toSet
      case None =>
        Set.empty
    }
  }

  def extractCodecs(lines: Iterator[String]): Set[String] =
    lines.flatMap(extractCodecs).toSet

  def extractCodecs(stream: InputStream): Set[String] = {
    val lines = scala.io.Source.fromInputStream(stream).getLines()
    extractCodecs(lines)
  }
}
