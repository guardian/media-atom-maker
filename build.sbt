import BuildVars._

scalaVersion in ThisBuild := "2.11.8"

name := "media-atom-maker"

organization in ThisBuild := "com.gu"

resolvers += "Guardian Bintray" at "https://dl.bintray.com/guardian/editorial-tools"

libraryDependencies ++= Seq(
  "ai.x"                       %% "diff"                         % "1.2.0",
  "org.apache.thrift"          %  "libthrift"                    % "0.9.3",
  "com.twitter"                %% "scrooge-core"                 % scroogeVersion,
  "com.twitter"                %% "scrooge-serializer"           % scroogeVersion,
  "com.amazonaws"              %  "aws-java-sdk-sts"             % awsVersion,
  "com.amazonaws"              %  "aws-java-sdk-ec2"             % awsVersion,
  "com.typesafe.scala-logging" %% "scala-logging"                % "3.4.0",
  "org.typelevel"              %% "cats-core"                    % "0.7.0", // for interacting with scanamo
  "com.fasterxml.jackson.core" %  "jackson-databind"             % "2.7.0",
  "org.cvogt"                  %% "play-json-extensions"         % "0.6.0",
  "com.gu"                     %% "pan-domain-auth-play_2-5"     % pandaVer,
  ws, // for panda
  "com.gu"                     %% "pan-domain-auth-verification" % pandaVer,
  "com.gu"                     %% "pan-domain-auth-core"         % pandaVer,
  "com.gu"                     %% "atom-publisher-lib"           % atomMakerVersion,
  "com.gu"                     %% "atom-publisher-lib"           % atomMakerVersion % "test" classifier "tests",
  "com.gu"                     %% "atom-manager-play"            % atomMakerVersion,
  "com.gu"                     %% "atom-manager-play"            % atomMakerVersion % "test" classifier "tests",
  "com.gu"                     %% "panda-hmac"                   % "1.1.0",
  "org.scalatestplus.play"     %% "scalatestplus-play"           % "1.5.0"   % "test",
  "org.mockito"                %  "mockito-core"                 % mockitoVersion % "test",
  "org.scala-lang.modules"     %% "scala-xml"                    % "1.0.5"   % "test",
  "com.google.api-client"      %  "google-api-client"            % "1.22.0",
  "com.google.apis"            % "google-api-services-youtube"   % "v3-rev178-1.22.0",
  "com.squareup.okhttp"        % "okhttp"                        % "2.4.0",
  cache,
  "net.logstash.logback"       % "logstash-logback-encoder"      % "4.8",
  "com.gu"                     % "kinesis-logback-appender"      % "1.3.0",
  "org.slf4j"                  % "slf4j-api"                     % "1.7.21",
  "org.slf4j"                  % "jcl-over-slf4j"                % "1.7.21",
  "com.gu"                     %% "content-atom-model"           %  "2.4.17"

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
  .enablePlugins(PlayScala, SbtWeb, RiffRaffArtifact, UniversalPlugin, BuildInfoPlugin)
  .settings(
    appDistSettings,
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      BuildInfoKey.constant("gitCommitId", Option(System.getenv("BUILD_VCS_NUMBER")) getOrElse(try {
        "git rev-parse HEAD".!!.trim
      } catch {
        case e: Exception => "unknown"
      }))
    ),
    buildInfoPackage := "app"
  )
