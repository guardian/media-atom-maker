package com.gu.media.upload.mediaconvert

case class Dimensions(width: Option[Int], height: Option[Int])

sealed trait ResolutionConfig {
	def dimensions: Dimensions
	def nameModifier: String
}

object Resolution {
	case object High extends ResolutionConfig {
		val dimensions = Dimensions(None, Some(720))
		val nameModifier = "_720h"
	}

	case object Low extends ResolutionConfig {
		val dimensions = Dimensions(Some(480), None)
		val nameModifier = "_480w"
	}
}
