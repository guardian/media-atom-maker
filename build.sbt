import StateMachine._
import sbtbuildinfo.BuildInfoPlugin.autoImport.{BuildInfoKey, buildInfoKeys}
import scala.sys.process._

val scroogeVersion = "4.12.0"
val awsVersion = "1.11.678"
val awsV2Version = "2.21.17"
val pandaVersion = "1.2.0"
val pandaHmacVersion = "2.1.0"
val atomMakerVersion = "1.3.4"
val typesafeConfigVersion = "1.4.0" // to match what we get from Play transitively
val scanamoVersion = "1.0.0-M28"

val playJsonExtensionsVersion = "0.40.2"
val okHttpVersion = "2.4.0"
val capiAwsVersion = "0.5"

val scalaTestVersion = "3.0.8"
val scalaTestPlusPlayVersion = "4.0.3"
val mockitoVersion = "2.0.97-beta"
val scalaXmlVersion = "1.0.5"
val scalaCheckVersion = "1.14.0" // to match ScalaTest version

val awsLambdaCoreVersion = "1.1.0"
val awsLambdaEventsVersion = "1.3.0"

val logbackClassicVersion = "1.2.3"
val logstashLogbackEncoderVersion = "4.8"

val permissionsClientVersion = "0.8"

val guavaVersion = "31.1-jre"
val googleOauthVersion = "1.33.3"
val googleHttpJacksonVersion = "1.41.7"
val commonsLoggingVersion = "1.1.1"
val apacheHttpClientVersion = "4.0.1"
val apacheHttpCoreVersion = "4.0.1"

val googleApiClientVersion = "1.35.2"
val youTubeApiClientVersion = "v3-rev178-1.22.0"

val jsoupVersion = "1.16.1"

val enumeratumVersion = "1.5.15"

lazy val jacksonVersion = "2.13.4"
lazy val jacksonDatabindVersion = "2.13.4.2"

lazy val commonSettings = Seq(
  ThisBuild / scalaVersion := "2.12.16",
  scalacOptions ++= Seq("-feature", "-deprecation"/*, "-Xfatal-warnings"*/),
  ThisBuild / organization := "com.gu",

  resolvers ++= Seq(
    "Sonatype OSS" at "https://oss.sonatype.org/content/repositories/releases/",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
  ),

  // silly SBT command to work-around lack of support for root projects that are not in the "root" folder
  // https://github.com/sbt/sbt/issues/2405
  Global / onLoad := (Global/ onLoad).value andThen (Command.process("project root", _))
)

// these Jackson dependencies are required to resolve issues in Play 2.8.x https://github.com/orgs/playframework/discussions/11222
val jacksonOverrides = Seq(
  "com.fasterxml.jackson.core" % "jackson-core",
  "com.fasterxml.jackson.core" % "jackson-annotations",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310",
  "com.fasterxml.jackson.module" %% "jackson-module-scala",
).map(_ % jacksonVersion)

val jacksonDatabindOverrides = Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion
)

lazy val common = (project in file("common"))
  .settings(commonSettings,
    name := "media-atom-common",
    unmanagedBase := baseDirectory.value / "common" / "lib",
    //YouTube Content ID API - Client Libraries. Only available to be download as JAR files.
    //Latest can be found here: https://developers.google.com/youtube/partner/client_libraries
    Compile / unmanagedJars += file("common/lib/google-api-services-youtubePartner-v1-rev20190401-1.25.0-sources.jar"),
    Compile / unmanagedJars += file("common/lib/google-api-services-youtubePartner-v1-rev20190401-1.25.0.jar"),
    libraryDependencies ++= Seq(
      "com.google.api-client" %  "google-api-client" % googleApiClientVersion,
      "com.google.http-client" % "google-http-client-jackson2" % googleHttpJacksonVersion,
      "com.google.apis" % "google-api-services-youtube" % youTubeApiClientVersion,
      "com.gu" %% "pan-domain-auth-play_2-8" % pandaVersion,
      "com.gu" %% "pan-domain-auth-verification" % pandaVersion,
      "com.gu" %% "pan-domain-auth-core" % pandaVersion,
      "com.gu" %% "panda-hmac-play_2-8" % pandaHmacVersion,
      ws,
      "com.typesafe.play" %% "play-json-joda" % "2.7.4",
      "com.gu" %% "atom-publisher-lib" % atomMakerVersion,
      "com.gu" %% "atom-publisher-lib" % atomMakerVersion % "test" classifier "tests",
      "com.gu" %% "atom-manager-play" % atomMakerVersion,
      "com.gu"  %% "atom-manager-play" % atomMakerVersion % "test" classifier "tests",
      "com.google.guava" % "guava" % guavaVersion,
      "com.google.oauth-client" % "google-oauth-client-jetty" % googleOauthVersion,
      "commons-logging" % "commons-logging" % commonsLoggingVersion,
      "org.apache.httpcomponents" % "httpclient" % apacheHttpClientVersion,
      "org.apache.httpcomponents" % "httpcore" % apacheHttpCoreVersion,
      "com.typesafe" % "config" % typesafeConfigVersion,
      "com.amazonaws" % "aws-lambda-java-core" % awsLambdaCoreVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
      "com.amazonaws" % "aws-java-sdk-dynamodb" % awsVersion,
      "software.amazon.awssdk" % "dynamodb" % awsV2Version,
      "com.amazonaws" % "aws-java-sdk-kinesis" % awsVersion,
      "ai.x" %% "play-json-extensions" % playJsonExtensionsVersion,
      "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
      "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
      "software.amazon.awssdk" % "sts" % awsV2Version,
      "com.amazonaws" % "aws-java-sdk-elastictranscoder" % awsVersion,
      "org.scanamo" %% "scanamo" % scanamoVersion,
      "com.squareup.okhttp" % "okhttp" % okHttpVersion,
      "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test", // to match ScalaTest version
      "com.amazonaws" % "aws-java-sdk-sns" % awsVersion,
      "com.amazonaws" % "aws-java-sdk-sqs" % awsVersion,
      "com.gu" %% "editorial-permissions-client" % permissionsClientVersion,
      "com.amazonaws" % "aws-java-sdk-stepfunctions" % awsVersion,
      "com.amazonaws" % "aws-java-sdk-ses" % awsVersion,
      "com.gu" %% "content-api-client-aws" % capiAwsVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.jsoup" % "jsoup" % jsoupVersion,
      "com.beachape" %% "enumeratum" % enumeratumVersion,
      "net.logstash.logback" % "logstash-logback-encoder" % "6.6"
    ) ++ jacksonOverrides ++ jacksonDatabindOverrides
  )

lazy val app = (project in file("."))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(PlayScala, SbtWeb, JDebPackaging, SystemdPlugin, BuildInfoPlugin)
  .settings(commonSettings,
    name := "media-atom-maker",
    libraryDependencies ++= Seq(
      ehcache,
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion,
      "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
      "software.amazon.awssdk" % "sts" % awsV2Version,
      "com.amazonaws" % "aws-java-sdk-ec2" % awsVersion,
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % "test",
      "org.mockito" %  "mockito-core" % mockitoVersion % "test",
      "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion   % "test"
    ),

    run / aggregate := false,

    bashScriptConfigLocation := Some("/etc/gu/media-atom-maker.ini"),

    buildInfoKeys := Seq[BuildInfoKey](
      name,
      "gitCommitId" -> Option(System.getenv("BUILD_VCS_NUMBER")).getOrElse(try {
        "git rev-parse HEAD".!!.trim
      } catch {
        case e: Exception => "unknown"
      })
    ),

    buildInfoPackage := "app",

    debianPackageDependencies := Seq("openjdk-8-jre-headless"),
    maintainer := "Digital CMS <digitalcms.dev@guardian.co.uk>",
    packageSummary := "media-atom-maker",
    packageDescription := """making media atoms""",
    pipelineStages := Seq(digest, gzip)
  )

lazy val uploader = (project in file("uploader"))
  .dependsOn(common)
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings,
    name := "media-atom-uploader",
    libraryDependencies ++= Seq(
      "net.logstash.logback" % "logstash-logback-encoder" % logstashLogbackEncoderVersion,
      "com.amazonaws" % "aws-lambda-java-events" % awsLambdaEventsVersion
    ),
    Universal / topLevelDirectory := None,
    Universal / packageName := normalizedName.value,

    Compile / lambdas := Map(
      "GetChunkFromS3" -> LambdaConfig(
        description = "Checks to see if a chunk of video has been uploaded to S3"
      ),
      "UploadChunkToYouTube" -> LambdaConfig(
        description = "Uploads a chunk of video to YouTube"
      ),
      "MultipartCopyChunkInS3" -> LambdaConfig(
        description = "Uses multipart copy to combine all the chunks in S3 into a single key"
      ),
      "CompleteMultipartCopy" -> LambdaConfig(
        description = "Finishes the multipart copy and deletes the source chunks from S3"
      ),
      "SendToPluto" -> LambdaConfig(
        description = "Sends a complete video to Pluto for ingestion"
      ),
      "SendToTranscoder" -> LambdaConfig(
        description = "Sends a complete video to the AWS transcoder"
      ),
      "GetTranscodingProgress" -> LambdaConfig(
        description = "Polls the AWS transcoder"
      ),
      "AddAssetToAtom" -> LambdaConfig(
        description = "Adds the resulting asset to the atom"
      ),
      "AddUploadDataToCache" -> LambdaConfig(
        description = "Adds the upload information to a Dynamo table so it is preserved even if the pipeline changes"
      )
    ),

    Compile / resourceGenerators += compileTemplate.taskValue
  )

lazy val integrationTests = (project in file("integration-tests"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(commonSettings,
    name := "integration-tests",
    Test / logBuffered := false,
    Test / parallelExecution := false
  )


lazy val expirer = (project in file("expirer"))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings,
    name := "media-atom-expirer",
    Universal / topLevelDirectory := None,
    Universal / packageName := normalizedName.value

  )

lazy val scheduler = (project in file("scheduler"))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings,
    name := "media-atom-scheduler",
    Universal / topLevelDirectory := None,
    Universal / packageName := normalizedName.value

  )

lazy val root = (project in file("root"))
  .aggregate(common, app, uploader, expirer, scheduler)
  .enablePlugins(RiffRaffArtifact)
  .settings(
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffManifestProjectName := "media-service:media-atom-maker",
    riffRaffArtifactResources := Seq(
      (app / Debian / packageBin).value -> s"${(app / name).value}/${(app / name).value}.deb",
      (uploader / Universal / packageBin).value -> s"media-atom-upload-actions/${(uploader / Universal / packageBin).value.getName}",
      (expirer / Universal / packageBin).value -> s"${(expirer / name).value}/${(expirer / Universal / packageBin).value.getName}",
      (scheduler / Universal / packageBin).value -> s"${(scheduler / name).value}/${(scheduler / Universal / packageBin).value.getName}",
      (app / baseDirectory).value / "pluto-message-ingestion/target/pluto-message-ingestion.zip" -> "pluto-message-ingestion/pluto-message-ingestion.zip",
      (app / baseDirectory).value / "conf/riff-raff.yaml" -> "riff-raff.yaml",
      (uploader / Compile / resourceManaged).value / "media-atom-pipeline.yaml" -> "media-atom-pipeline-cloudformation/media-atom-pipeline.yaml"
    )
  )
