package com.gu.media

import com.typesafe.config.Config
import scala.jdk.CollectionConverters._

trait Settings {
  def config: Config

  def getString(name: String): Option[String] = if (config.hasPath(name)) {
    Some(config.getString(name))
  } else { None }
  def getStringSet(name: String): Set[String] = if (config.hasPath(name)) {
    config.getStringList(name).asScala.toSet
  } else { Set.empty }
  def getBoolean(name: String): Option[Boolean] = if (config.hasPath(name)) {
    Some(config.getBoolean(name))
  } else { None }

  def getMandatoryString(name: String, hint: String = ""): String = if (
    config.hasPath(name)
  ) {
    config.getString(name)
  } else {
    throw new IllegalArgumentException(s"Missing $name $hint")
  }
}

object Settings {
  def apply(raw: Config): Settings = new Settings {
    override def config: Config = raw
  }
}
