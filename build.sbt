import BuildVars._

scalaVersion in ThisBuild := "2.11.8"

name := "media-atom-maker"

organization in ThisBuild := "com.gu"

libraryDependencies ++= Seq(
  "com.gu"                     %% "content-atom-model"           % contentAtomVersion,
  "org.apache.thrift"          %  "libthrift"                    % "0.9.3",
  "com.twitter"                %% "scrooge-core"                 % scroogeVersion,
  "com.twitter"                %% "scrooge-serializer"           % scroogeVersion,
  "com.amazonaws"              % "aws-java-sdk-sts"              % awsVersion,
  "com.typesafe.scala-logging" %% "scala-logging"                % "3.4.0",
  "org.typelevel"              %% "cats-core"                    % "0.6.0", // for interacting with scanamo
  "com.fasterxml.jackson.core" %  "jackson-databind"             % "2.7.0",
  "com.gu"                     %% "pan-domain-auth-play_2-5"     % pandaVer,
  ws, // for panda
  "com.gu"                     %% "pan-domain-auth-verification" % pandaVer,
  "com.gu"                     %% "pan-domain-auth-core"         % pandaVer,
  "com.gu"                     %% "atom-publisher-lib"           % "0.1.3",
  "com.gu"                     %% "atom-publisher-lib"           % "0.1.3" % "test" classifier "tests",
  "com.gu"                     %% "atom-manager-play"            % "0.1.3",
  "com.gu"                     %% "atom-manager-play"            % "0.1.3" % "test" classifier "tests",
  "org.scalatestplus.play"     %% "scalatestplus-play"           % "1.5.0"   % "test",
  "org.mockito"                %  "mockito-core"                 % mockitoVersion % "test",
  "org.scala-lang.modules"     %% "scala-xml"                    % "1.0.5"   % "test"
) ++ scanamoDeps

lazy val appDistSettings = Seq(
    packageName in Universal := name.value,
    riffRaffPackageType := (packageZipTarball in Universal).value,
    riffRaffBuildIdentifier := Option(System.getenv("CIRCLE_BUILD_NUM")).getOrElse("dev"),
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffArtifactPublishPath := name.value,
    riffRaffPackageName := s"media-service:${name.value}",
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
  .enablePlugins(PlayScala, SbtWeb, RiffRaffArtifact, UniversalPlugin)
  .settings(appDistSettings)
