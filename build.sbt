scalaVersion := "2.11.8"

name := "media-atom-maker"

version := "1.0.0-SNAPSHOT"

lazy val contentAtomVersion = "1.0.1"
lazy val scroogeVersion     = "4.2.0"
lazy val AwsSdkVersion      = "1.10.74"

libraryDependencies ++= Seq(
  "com.gu"                     %% "content-atom-model"   % contentAtomVersion,
  "com.amazonaws"              % "aws-java-sdk-kinesis"  % AwsSdkVersion,
  "com.amazonaws"              % "aws-java-sdk-dynamodb" % AwsSdkVersion,
  "org.apache.thrift"          % "libthrift"             % "0.9.3",
  "com.twitter"                %% "scrooge-core"         % scroogeVersion,
  "com.twitter"                %% "scrooge-serializer"   % scroogeVersion,
  "com.gu"                     %% "scanamo"              % "0.5.0",
  "org.typelevel"              %% "cats-core"            % "0.6.0", // for interacting with scanamo
  "com.fasterxml.jackson.core" % "jackson-databind"      % "2.7.0",
  "org.scalatestplus.play"     %% "scalatestplus-play"   % "1.5.0"   % "test",
  "org.mockito"                % "mockito-core"          % "1.10.19" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
