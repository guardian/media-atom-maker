package com.gu.media.util

import scala.annotation.tailrec

/**
 * image/video aspect ratio calculation copied from guardian/grid
 */
object AspectRatio {
  case class Ratio(name: String, width: Int, height: Int)

  val knownRatios: List[Ratio] = List(
    // square
    Ratio("1:1", 1, 1),
    // portrait
    Ratio("2:3", 2, 3),
    Ratio("3:4", 3, 4),
    Ratio("3:5", 3, 5),
    Ratio("4:5", 4, 5),
    Ratio("9:16", 9, 16),
    // landscape
    Ratio("3:2", 3, 2),
    Ratio("4:3", 4, 3),
    Ratio("5:3", 5, 3),
    Ratio("5:4", 5, 4),
    Ratio("16:9", 16, 9),
  )

  @tailrec
  private def gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

  def calculate(width: Int, height: Int, tolerance: Int = 3) : Option[Ratio] = {
    val matchingRatio = for {
      w <- width - tolerance until width + tolerance
      h <- height - tolerance until height + tolerance
      g = gcd(w, h)
      simplifiedWidth = w / g
      simplifiedHeight = h / g
      ratio <- knownRatios.find(ratio => ratio.width == simplifiedWidth &&  ratio.height == simplifiedHeight)
    } yield ratio
    matchingRatio.headOption
  }
}
