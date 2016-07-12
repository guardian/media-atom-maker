scalaVersion := "2.11.8"

name := "atom-publisher-lib"

version := "1.0.0-SNAPSHOT"

lazy val contentAtomVersion = "1.0.1"
lazy val scroogeVersion     = "4.2.0"
lazy val AwsSdkVersion      = "1.10.74"
lazy val pandaVer           = "0.3.0"

libraryDependencies ++= Seq(
  "com.gu"                     %% "content-atom-model"           % contentAtomVersion,
  "com.amazonaws"              %  "aws-java-sdk-kinesis"         % AwsSdkVersion,
  "com.typesafe.scala-logging" %% "scala-logging"                % "3.4.0",
  "com.twitter"                %% "scrooge-serializer"           % scroogeVersion,
  "com.twitter"                %% "scrooge-core"                 % scroogeVersion,
  "org.mockito"                %  "mockito-core"                 % "1.10.19" % "test",
  "org.scalatest"              %% "scalatest"                    % "2.2.6"   % "test"
)
