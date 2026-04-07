package com.gu.media.upload

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

import java.io.File

class FfMpegTest extends AnyFunSuite with Matchers {

  // Get the ffmpeg binary path, trying common locations first
  private def getFfmpegPath: String = {
    val commonPaths = List(
      "/opt/homebrew/bin/ffmpeg", // macOS with Homebrew
      "/usr/local/bin/ffmpeg", // macOS/Linux common location
      "/var/task/bin/ffmpeg" // AWS Lambda
    )

    commonPaths
      .find(path => new File(path).exists())
      .getOrElse(
        "ffmpeg"
      )
  }

  val ffMpegPath = getFfmpegPath

  test("checkAudioExists returns true for video with audio") {
    println(ffMpegPath)
    val videoWithAudio =
      "https://uploads.guim.co.uk/2026/04/01/010426_tif_latest_front_loop--934995e6-c29d-4525-9691-d8cd63bd0588-1.2_480w.mp4"

    FfMpeg.checkAudioExists(videoWithAudio, ffMpegPath) must be(true)
  }

  test("checkAudioExists returns false for a video with no audiotracks") {
    val videoWithoutAudioTracks =
      "https://uploads.guim.co.uk/2026/03/31/Brainrot-4-5--1d023563-efee-451b-9b8c-d7c7f6a4cc60-1.0_480w.mp4"

    FfMpeg.checkAudioExists(videoWithoutAudioTracks, ffMpegPath) must be(false)
  }

  test("checkAudioExists returns false for video with audio that is silent") {
    val video =
      "https://uploads.guim.co.uk/2026/03/18/Insect_migration--5c3d77d2-3557-4c73-a27d-7aab1f864cdc-1.0.mp4"

    FfMpeg.checkAudioExists(video, ffMpegPath) must be(false)
  }
}
