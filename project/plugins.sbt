// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.3")

addSbtPlugin("com.gu" % "sbt-riffraff-artifact" % "0.8.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")
