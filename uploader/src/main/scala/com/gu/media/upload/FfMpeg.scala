package com.gu.media.upload

import com.gu.media.logging.Logging

import java.nio.file.Path
import scala.sys.process.{Process, ProcessLogger, stdout}
import scala.collection.mutable
import scala.util.matching.Regex

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

    /* @see https://aws.amazon.com/blogs/media/detect-silent-audio-tracks-in-vod-content-with-aws-elemental-mediaconvert/ */
    val SILENT_THRESHOLD = -50

    val ffMpegStdErrLogger = new BasicStdErrLogger()

    val cmd =
      s"${ffmpegPath} -seekable 1 -i \"$video\" -filter:a volumedetect -f null /dev/null"
    val exitCode = Process(cmd, cwd = None).!(
      ProcessLogger(stdout.append(_), ffMpegStdErrLogger.append)
    )

    def findRegexMatch(regex: Regex, output: String) = {
      regex
        .findFirstMatchIn(output)
    }

    exitCode match {
      case 0 =>
        val output = ffMpegStdErrLogger.getOutput;

        val maxVolumeRegex = """max_volume:\s*([-\d.]+)\s*dB""".r
        val audioStreamRegex = """Stream .*: Audio""".r
        val videoStreamRegex = """Stream .*: Video""".r

        val maxVolume = findRegexMatch(maxVolumeRegex, output)
          .map(_.group(1).toDouble)
        val audioStream = findRegexMatch(audioStreamRegex, output)
        val videoStream = findRegexMatch(videoStreamRegex, output)

        (maxVolume, audioStream, videoStream) match {

          case (Some(db), _, _) =>
            db > SILENT_THRESHOLD /* We treat near-silence as no audio */
          case (None, None, Some(video)) =>
            false /* Couldn't parse volume, audio streams are absent, but video streams are present. We assume from this that the video is silent */
          case (None, None, None) =>
            true /* Couldn't parse volume and there are no audio or video streams, so we return true for safety*/
        }
      case _ =>
        log.error("FfMpeg audio detection failed")
        true /* Audio detection failure is not a critical error, so we return true rather than throwing an exception */
    }
  }

}
