scalaVersion := "2.11.8"

name := "media-atom-maker"

version := "1.0.0-SNAPSHOT"

lazy val contentAtomVersion = "1.0.1"

libraryDependencies ++= Seq(
  "com.gu" %% "content-atom-model" % contentAtomVersion,
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
