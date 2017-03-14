package com.gu.media

import com.typesafe.config.Config
import scala.collection.JavaConverters._

trait Settings {
  def config: Config

  def getString(name: String): Option[String] = if(config.hasPath(name)) { Some(config.getString(name)) } else { None }
  def getStringList(name: String): List[String] = if(config.hasPath(name)) { config.getStringList(name).asScala.toList } else { List.empty}
  def getBoolean(name: String): Option[Boolean] = if(config.hasPath(name)) { Some(config.getBoolean(name)) } else { None }

  def getMandatoryString(name: String, hint: String = ""): String = if(config.hasPath(name)) {
    config.getString(name)
  } else {
    throw new IllegalArgumentException(s"Missing $name $hint")
  }
}
