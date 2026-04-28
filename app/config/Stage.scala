package config

sealed trait Stage {
  def name: String = this match {
    case Prod => "PROD"
    case Code => "CODE"
    case Dev => "DEV"
  }

  override def toString: String = name
}

object Stage {
  def apply(value: String): Stage = value.toUpperCase match {
    case "PROD" => Prod
    case "CODE" => Code
    case "DEV" => Dev
    case other => throw new IllegalStateException(s"invalid stage: $other")
  }
}

object Prod extends Stage
object Code extends Stage
object Dev extends Stage
