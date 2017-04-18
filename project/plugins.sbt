// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"

// a faster, alternative, dependancy resolver to ivy
// https://github.com/coursier/coursier#sbt-plugin
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC1")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("com.gu" % "sbt-riffraff-artifact" % "0.9.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.5")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")

// for creating test cases that use a local dynamodb

addSbtPlugin("com.localytics" % "sbt-dynamodb" % "1.4.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))
