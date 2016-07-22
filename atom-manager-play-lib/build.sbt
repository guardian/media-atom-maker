lazy val contentAtomVersion = "1.0.1"

lazy val playVersion = "2.5.3"

lazy val AwsSdkVersion = "1.10.74"

name := "atom-manager-play"

libraryDependencies ++= Seq(
  "com.typesafe.play"      %% "play"                  % playVersion,
  "com.gu"                 %% "content-atom-model"    % contentAtomVersion,
  "org.typelevel"          %% "cats-core"             % "0.6.0",
  "com.gu"                 %% "scanamo"               % "0.6.1-SNAPSHOT",
  "com.gu"                 %% "scanamo-scrooge"       % "0.1.2-SNAPSHOT",
  "org.scalatestplus.play" %% "scalatestplus-play"    % "1.5.0"   % "test",
  "com.amazonaws"          %  "aws-java-sdk-dynamodb" % AwsSdkVersion,
  "org.mockito"            %  "mockito-core"          % "1.10.19" % "test"
    //"com.typesafe.play" %% "play-ws" % playVersion
)
