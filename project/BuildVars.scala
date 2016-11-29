import sbt._

object BuildVars {
  lazy val awsVersion         = "1.11.48"
  lazy val contentAtomVersion = "2.4.16"
  lazy val scroogeVersion     = "4.2.0"
  lazy val pandaVer           = "0.3.0"
  lazy val mockitoVersion     = "2.0.97-beta"
  lazy val atomMakerVersion   = "0.1.5"

  lazy val scanamoDeps = Seq(
    "com.gu"                     %% "scanamo"              % "0.7.0",
    "com.gu"                     %% "scanamo-scrooge"      % "0.1.3"
  )
}
