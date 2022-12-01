// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"

// a, faster, alternative dependancy resolver to ivy
// https://github.com/coursier/coursier#sbt-plugin
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC12")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

addSbtPlugin("com.gu" % "sbt-riffraff-artifact" % "1.1.17")

// for creating test cases that use a local dynamodb

// FIXME unmaintained and archived
addSbtPlugin("com.localytics" % "sbt-dynamodb" % "2.0.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")

libraryDependencies += "org.vafer" % "jdeb" % "1.6" artifacts (Artifact("jdeb", "jar", "jar"))
