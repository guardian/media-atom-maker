import BuildVars._

scalaVersion := "2.11.8"

name := "atom-publisher-lib"

version := "1.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.gu"                     %% "content-atom-model"   % contentAtomVersion,
  "com.amazonaws"              %  "aws-java-sdk-kinesis" % awsVersion,
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.4.0",
  "com.twitter"                %% "scrooge-serializer"   % scroogeVersion,
  "com.twitter"                %% "scrooge-core"         % scroogeVersion,
  "com.typesafe.akka"          %% "akka-actor"           % akkaVersion,
  "org.mockito"                %  "mockito-core"         % "1.10.19"   % "test",
  "org.scalatest"              %% "scalatest"            % "2.2.6"     % "test",
  "com.typesafe.akka"          %% "akka-testkit"         % akkaVersion % "test"
) ++  scanamoDeps
