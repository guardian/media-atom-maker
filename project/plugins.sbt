// Use the Play sbt plugin for Play projects
addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.1")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")

// for creating test cases that use a local dynamodb

// FIXME unmaintained and archived
addSbtPlugin("com.localytics" % "sbt-dynamodb" % "2.0.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")

libraryDependencies += "org.vafer" % "jdeb" % "1.6" artifacts (Artifact(
  "jdeb",
  "jar",
  "jar"
))

addDependencyTreePlugin
