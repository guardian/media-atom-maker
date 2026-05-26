package com.gu.media.util

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
    Ratio("16:9", 16, 9)
  )

  // A tolerance of zero means that for a given width (or height), the height (or width) is the closest
  // whole number to the exact aspect ratio. e.g. 480x854 is not exactly 9:16, but it is closer than
  // 480x853 or 480x855, so it cannot be improved.
  //
  // A tolerance of one means that for a given width (or height), the height (or width) is at most 1px
  // away from the closest integer to the exact aspect ratio. e.g. 480x855 is not exactly 9:16, and
  // 480x854 is closer to 9:16, but 480x855 will be accepted as 9:16 if the tolerance is one.
  //
  // Unfortunately this is slightly complicated by allowing a tolerance on both the width and height.
  // As a result 480x855 is also accepted as 9:16 with a tolerance of *zero* because 480x855 is the
  // closest approximation to 9:16 when the height is fixed at 855.

  def calculate(width: Int, height: Int, tolerance: Int = 1): Option[Ratio] = {
    // Assume that the height is fixed, and desired ratio was achieved by adjusting the width.
    // What range of aspect ratios would be possible within the tolerance?
    val fixedHeightLowerBound = (width - tolerance - 1).toDouble / height
    val fixedHeightUpperBound = (width + tolerance + 1).toDouble / height

    // Assume that the width is fixed, and desired ratio was achieved by adjusting the height.
    // What range of aspect ratios would be possible within the tolerance?
    val fixedWidthLowerBound = width.toDouble / (height + tolerance + 1)
    val fixedWidthUpperBound = width.toDouble / (height - tolerance - 1)

    knownRatios
      .flatMap({ knownRatio =>
        val knownDouble = knownRatio.width.toDouble / knownRatio.height
        if (
          fixedWidthLowerBound < knownDouble && knownDouble < fixedWidthUpperBound ||
          fixedHeightLowerBound < knownDouble && knownDouble < fixedHeightUpperBound
        ) {
          Some((knownRatio, Math.abs(knownDouble - (width.toDouble / height))))
        } else { None }
      })
      .sortBy(_._2) // sort by error from the exact aspect ratio, so that the closest match is first
      .headOption
      .map(_._1) // return the aspect ratio only
  }
}
