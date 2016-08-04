import sbt._

object BuildVars {
  lazy val awsVersion         = "1.11.8"
  lazy val contentAtomVersion = "2.4.0"
  lazy val scroogeVersion     = "4.2.0"
  lazy val akkaVersion        = "2.4.8"
  lazy val pandaVer           = "0.3.0"
  lazy val playVersion        = "2.5.3"
  lazy val mockitoVersion     = "2.0.97-beta"

  lazy val scanamoDeps = Seq(
    "com.gu"                     %% "scanamo"              % "0.6.0",
    "com.gu"                     %% "scanamo-scrooge"      % "0.1.2"
  )
}
