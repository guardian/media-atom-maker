scalaVersion := "2.11.8"

name := "media-atom-maker"

version := "1.0.0-SNAPSHOT"

lazy val contentAtomVersion = "1.0.1"
lazy val scroogeVersion     = "4.2.0"
lazy val AwsSdkVersion      = "1.10.74"
lazy val pandaVer           = "0.3.0"

libraryDependencies ++= Seq(
  "com.gu"                     %% "content-atom-model"           % contentAtomVersion,
  "com.amazonaws"              %  "aws-java-sdk-kinesis"         % AwsSdkVersion,
  "com.amazonaws"              %  "aws-java-sdk-dynamodb"        % AwsSdkVersion,
  "org.apache.thrift"          %  "libthrift"                    % "0.9.3",
  "com.twitter"                %% "scrooge-core"                 % scroogeVersion,
  "com.twitter"                %% "scrooge-serializer"           % scroogeVersion,
  "com.gu"                     %% "scanamo"                      % "0.5.0",
  "com.typesafe.scala-logging" %% "scala-logging"                % "3.4.0",
  "org.typelevel"              %% "cats-core"                    % "0.6.0", // for interacting with scanamo
  "com.fasterxml.jackson.core" %  "jackson-databind"             % "2.7.0",
  "com.gu"                     %% "pan-domain-auth-play_2-5"     % pandaVer,
  ws, // for panda
  "com.gu"                     %% "pan-domain-auth-verification" % pandaVer,
  "com.gu"                     %% "pan-domain-auth-core"         % pandaVer,
  "org.scalatestplus.play"     %% "scalatestplus-play"           % "1.5.0"   % "test",
  "org.mockito"                %  "mockito-core"                 % "1.10.19" % "test",
  "org.scala-lang.modules"     %% "scala-xml"                    % "1.0.5"   % "test"
)

lazy val appDistSettings = Seq(
    packageName in Universal := name.value,
    riffRaffPackageType := (packageZipTarball in Universal).value,
    riffRaffBuildIdentifier := Option(System.getenv("CIRCLE_BUILD_NUM")).getOrElse("dev"),
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffArtifactPublishPath := name.value,
    riffRaffPackageName := s"editorial-tools:${name.value}",
    riffRaffManifestProjectName := riffRaffPackageName.value,
    riffRaffArtifactResources := Seq(
      riffRaffPackageType.value -> s"packages/${name.value}/${name.value}.tgz",
      baseDirectory.value / "conf" / "deploy.json" -> "deploy.json"
    ),
    artifactName in Universal := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      artifact.name + "." + artifact.extension
    },
    riffRaffManifestBranch := Option(System.getenv("CIRCLE_BRANCH")).getOrElse("dev")
  )

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, RiffRaffArtifact, UniversalPlugin)
  .settings(appDistSettings)
