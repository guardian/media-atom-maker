lazy val contentAtomVersion = "2.4.0"

lazy val playVersion = "2.5.3"

name := "atom-manager-play"

libraryDependencies ++= Seq(
  "com.typesafe.play"      %% "play"               % playVersion,
  "com.gu"                 %% "content-atom-model" % contentAtomVersion,
  "org.typelevel"          %% "cats-core"          % "0.6.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0"   % "test",
  "org.mockito"            %  "mockito-core"       % "1.10.19" % "test"
    //"com.typesafe.play" %% "play-ws" % playVersion
)
