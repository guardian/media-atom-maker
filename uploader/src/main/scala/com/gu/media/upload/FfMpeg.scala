package com.gu.media.upload

import com.gu.media.logging.Logging
import java.nio.file.Path
import scala.sys.process.{Process, ProcessLogger, stdout}

import scala.collection.mutable

class BasicStdErrLogger extends Logging {
  private val acc = mutable.Buffer[String]()

  def append(line: String): Unit = {
    acc.append(line)

    log.info(line)
  }

  def getOutput: String = {
    acc.mkString("\n")
  }
}
object FfMpeg extends Logging {

  private case class FfMpegSubprocessCrashedException(
      exitCode: Int,
      stderr: String
  ) extends Exception(s"Exit code: $exitCode: ${stderr}")

  def addSubtitlesToMP4(video: Path, subtitles: Path, output: Path): Path = {
    val ffMpegStdErrLogger = new BasicStdErrLogger()

    val cmd =
      s"/var/task/bin/ffmpeg -i \"$video\" -i \"$subtitles\" -c copy -c:s mov_text -metadata:s:s:0 language=eng \"${output.toString}\""
    val exitCode = Process(cmd, cwd = None).!(
      ProcessLogger(stdout.append(_), ffMpegStdErrLogger.append)
    )

    exitCode match {
      case 0 =>
        output
      case _ =>
        log.error("FfMpeg conversion in transcription extraction failed")
        throw FfMpegSubprocessCrashedException(
          exitCode,
          ffMpegStdErrLogger.getOutput
        )
    }
  }
}
