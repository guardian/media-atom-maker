lazy val contentAtomVersion = "1.0.1"

lazy val playVersion = "2.5.3"

name := "atom-manager-play"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play"               % playVersion,
  "com.gu"            %% "content-atom-model" % contentAtomVersion,
  "org.typelevel"     %% "cats-core"          % "0.6.0"
  //"com.typesafe.play" %% "play-ws" % playVersion
)
