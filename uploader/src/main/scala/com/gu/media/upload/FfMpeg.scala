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
  private val AWSLambdaFfmpegPath = "/var/task/bin/ffmpeg"

  private case class FfMpegSubprocessCrashedException(
      exitCode: Int,
      stderr: String
  ) extends Exception(s"Exit code: $exitCode: ${stderr}")

  def addSubtitlesToMP4(video: Path, subtitles: Path, output: Path): Path = {
    val ffMpegStdErrLogger = new BasicStdErrLogger()

    val cmd =
      s"${AWSLambdaFfmpegPath} -i \"$video\" -i \"$subtitles\" -c copy -c:s mov_text -metadata:s:s:0 language=eng \"${output.toString}\""
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

  def checkAudioExists(
      video: String,
      ffmpegPath: String = AWSLambdaFfmpegPath
  ): Boolean = {

    // @see https://aws.amazon.com/blogs/media/detect-silent-audio-tracks-in-vod-content-with-aws-elemental-mediaconvert/
    val SILENT_THRESHOLD = -50

    val ffMpegStdErrLogger = new BasicStdErrLogger()

    val cmd =
      s"${ffmpegPath} -seekable 1 -i \"$video\" -filter:a volumedetect -f null /dev/null"
    val exitCode = Process(cmd, cwd = None).!(
      ProcessLogger(stdout.append(_), ffMpegStdErrLogger.append)
    )

    exitCode match {
      case 0 =>
        val output = ffMpegStdErrLogger.getOutput;
        val maxVolumeRegex = """max_volume:\s*([-\d.]+)\s*dB""".r
        val maxVolume = maxVolumeRegex
          .findFirstMatchIn(output)
          .map(_.group(1).toDouble)

        maxVolume match {
          case Some(db) =>
            db > SILENT_THRESHOLD // treat near-silence as no audio
          case None => false // couldn't parse = assume no audio
        }
      case _ =>
        log.error("FfMpeg audio detection failed")
        true // audio detection failure is not a critical error, so we return true rather than throwing an exception
    }
  }

}
