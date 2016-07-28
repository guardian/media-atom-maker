scalaVersion := "2.11.8"

name := "atom-publisher-lib"

version := "1.0.0-SNAPSHOT"

lazy val contentAtomVersion = "1.0.1"
lazy val scroogeVersion     = "4.2.0"
lazy val AwsSdkVersion      = "1.11.8"
lazy val akkaVersion        = "2.4.8"

libraryDependencies ++= Seq(
  "com.gu"                     %% "content-atom-model"   % contentAtomVersion,
  "com.amazonaws"              %  "aws-java-sdk-kinesis" % AwsSdkVersion,
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.4.0",
  "com.gu"                     %% "scanamo"              % "0.6.1-SNAPSHOT",
  "com.gu"                     %% "scanamo-scrooge"      % "0.1.2-SNAPSHOT",
  "com.twitter"                %% "scrooge-serializer"   % scroogeVersion,
  "com.twitter"                %% "scrooge-core"         % scroogeVersion,
  "com.typesafe.akka"          %% "akka-actor"           % akkaVersion,
  "org.mockito"                %  "mockito-core"         % "1.10.19"   % "test",
  "org.scalatest"              %% "scalatest"            % "2.2.6"     % "test",
  "com.typesafe.akka"          %% "akka-testkit"         % akkaVersion % "test"
)
