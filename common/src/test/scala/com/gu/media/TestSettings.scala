package com.gu.media

trait TestSettings { this: Settings =>
  final override def getString(name: String): Option[String] = None
  final override def getBoolean(name: String): Option[Boolean] = None
  final override def getMandatoryString(name: String, hint: String): String = ""
}
