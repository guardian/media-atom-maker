// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.19")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

addSbtPlugin("com.gu" % "sbt-riffraff-artifact" % "1.1.18")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.1")

// for creating test cases that use a local dynamodb

// FIXME unmaintained and archived
addSbtPlugin("com.localytics" % "sbt-dynamodb" % "2.0.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")

libraryDependencies += "org.vafer" % "jdeb" % "1.6" artifacts (Artifact("jdeb", "jar", "jar"))

addDependencyTreePlugin

/*
   scala-xml has been updated to 2.x in sbt, but not in other sbt plugins like sbt-native-packager
   See: https://github.com/scala/bug/issues/12632
   This is effectively overrides the safeguards (early-semver) put in place by the library authors ensuring binary compatibility.
   We consider this a safe operation because when set under `projects/` (ie *not* in `build.sbt` itself) it only affects the
   compilation of build.sbt, not of the application build itself.
   Once the build has succeeded, there is no further risk (ie of a runtime exception due to clashing versions of `scala-xml`).
 */
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
