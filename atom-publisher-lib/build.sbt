import com.typesafe.sbt.SbtPgp.autoImportImpl._
import sbtrelease._

import ReleaseStateTransformations._


Sonatype.sonatypeSettings


name := "atom-publisher-lib"

organization := "com.gu"
scalaVersion := "2.11.8"
scmInfo := Some(ScmInfo(url("https://github.com/guardian/media-atom-maker"),
  "scm:git:git@github.com:guardian/media-atom-maker.git"))

pomExtra := (
  <url>https://github.com/guardian/media-atom-maker</url>
    <developers>
      <developer>
        <id>paulmr</id>
        <name>Paul Roberts</name>
        <url>https://github.com/paulmr</url>
      </developer>
    </developers>
  )
licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

releasePublishArtifactsAction := PgpKeys.publishSigned.value
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)


lazy val contentAtomVersion = "1.0.1"
lazy val scroogeVersion     = "4.2.0"
lazy val AwsSdkVersion      = "1.10.74"
lazy val akkaVersion        = "2.4.8"

libraryDependencies ++= Seq(
  "com.gu"                     %% "content-atom-model"   % contentAtomVersion,
  "com.amazonaws"              %  "aws-java-sdk-kinesis" % AwsSdkVersion,
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.4.0",
  "com.twitter"                %% "scrooge-serializer"   % scroogeVersion,
  "com.twitter"                %% "scrooge-core"         % scroogeVersion,
  "com.typesafe.akka"          %% "akka-actor"           % akkaVersion,
  "org.mockito"                %  "mockito-core"         % "1.10.19"   % "test",
  "org.scalatest"              %% "scalatest"            % "2.2.6"     % "test",
  "com.typesafe.akka"          %% "akka-testkit"         % akkaVersion % "test"
)
