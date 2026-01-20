import StateMachine.*
import sbtbuildinfo.BuildInfoPlugin.autoImport.{BuildInfoKey, buildInfoKeys}

import scala.collection.immutable.Seq
import scala.sys.process.*

val scroogeVersion = "4.12.0"
val awsVersion = "1.11.1034"
val awsV2Version = "2.32.26"
val pandaVersion = "13.0.0"
val atomMakerVersion = "9.0.0"
val typesafeConfigVersion =
  "1.4.0" // to match what we get from Play transitively
val scanamoVersion = "1.0.0-M28"

val playJsonExtensionsVersion = "1.0.3"
val okHttpVersion = "2.4.0"

val scalaTestVersion = "3.2.19"
val scalaTestPlusPlayVersion = "7.0.2"
val mockitoVersion = "2.0.0"
val scalaXmlVersion = "2.2.0"
val scalaCheckVersion = "1.18.0" // to match ScalaTest version

val awsLambdaCoreVersion = "1.1.0"
val awsLambdaEventsVersion = "1.3.0"

val logbackClassicVersion = "1.5.18"
val logstashLogbackEncoderVersion = "4.8"

val guavaVersion = "31.1-jre"
val googleOauthVersion = "1.34.1"
val googleHttpJacksonVersion = "1.43.3"
val commonsLoggingVersion = "1.1.1"
val apacheHttpClientVersion = "4.0.1"
val apacheHttpCoreVersion = "4.0.1"

val googleApiClientVersion = "2.2.0"
val youTubeApiClientVersion = "v3-rev20230123-2.0.0"

val jsoupVersion = "1.16.1"

val enumeratumVersion = "1.5.15"

lazy val jacksonVersion = "2.19.1"

lazy val commonSettings = Seq(
  ThisBuild / scalaVersion := "2.13.18",
  scalacOptions ++= Seq("-feature", "-deprecation", "-release:21"),
  ThisBuild / organization := "com.gu",
  resolvers ++= Resolver.sonatypeOssRepos("releases"),

  // silly SBT command to work-around lack of support for root projects that are not in the "root" folder
  // https://github.com/sbt/sbt/issues/2405
  Global / onLoad := (Global / onLoad).value andThen (Command
    .process("project root", _)),
  dependencyOverrides ++= jacksonOverrides
)

val jacksonOverrides = Seq(
  "com.fasterxml.jackson.core" % "jackson-core",
  "com.fasterxml.jackson.core" % "jackson-annotations",
  "com.fasterxml.jackson.core" % "jackson-databind",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310",
  "com.fasterxml.jackson.module" % "jackson-module-parameter-names",
  "com.fasterxml.jackson.module" %% "jackson-module-scala"
).map(_ % jacksonVersion)

lazy val common = (project in file("common"))
  .settings(
    commonSettings,
    name := "media-atom-common",
    unmanagedBase := baseDirectory.value / "common" / "lib",
    // YouTube Content ID API - Client Libraries. Not available for download.
    // Follow the instructions in the scripts/youtubepartner-api-gen directory to
    // regenerate+update these.
    Compile / unmanagedJars += file(
      "common/lib/google-api-services-youtubePartner-v1-rev20230804-2.0.0.jar"
    ),
    // Attaching source jars doesn't work as you'd expect :(
    // If you try to cmd+click to the definition of any classes from youtubePartner-api, Intellij will offer you an option to
    // "attach sources" -> do that and select the file below.
    // Compile / unmanagedJars += file("common/lib/google-api-services-youtubePartner-v1-rev20230804-2.0.0-sources.jar"),
    libraryDependencies ++= Seq(
      "com.google.api-client" % "google-api-client" % googleApiClientVersion,
      "com.google.http-client" % "google-http-client-jackson2" % googleHttpJacksonVersion,
      "com.google.apis" % "google-api-services-youtube" % youTubeApiClientVersion,
      "com.gu" %% "pan-domain-auth-play_3-0" % pandaVersion,
      "com.gu" %% "pan-domain-auth-verification" % pandaVersion,
      "com.gu" %% "pan-domain-auth-core" % pandaVersion,
      "com.gu" %% "panda-hmac-play_3-0" % pandaVersion,
      ws,
      "org.playframework" %% "play-json-joda" % "3.0.5",
      "com.gu" %% "atom-publisher-lib" % atomMakerVersion,
      "com.gu" %% "atom-publisher-lib" % atomMakerVersion % "test" classifier "tests",
      "com.gu" %% "atom-manager-play" % atomMakerVersion,
      "com.gu" %% "atom-manager-play" % atomMakerVersion % "test" classifier "tests",
      "com.google.guava" % "guava" % guavaVersion,
      "commons-logging" % "commons-logging" % commonsLoggingVersion,
      "org.apache.httpcomponents" % "httpclient" % apacheHttpClientVersion,
      "org.apache.httpcomponents" % "httpcore" % apacheHttpCoreVersion,
      "com.typesafe" % "config" % typesafeConfigVersion,
      "com.amazonaws" % "aws-lambda-java-core" % awsLambdaCoreVersion,
      "software.amazon.awssdk" % "dynamodb" % awsV2Version,
      "software.amazon.awssdk" % "kinesis" % awsV2Version,
      "com.gu" %% "play-json-extensions" % playJsonExtensionsVersion,
      "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
      "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
      "software.amazon.awssdk" % "sts" % awsV2Version,
      "software.amazon.awssdk" % "mediaconvert" % awsV2Version,
      "org.scanamo" %% "scanamo" % scanamoVersion,
      "com.squareup.okhttp" % "okhttp" % okHttpVersion,
      "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test", // to match ScalaTest version
      "software.amazon.awssdk" % "sns" % awsV2Version,
      "software.amazon.awssdk" % "sqs" % awsV2Version,
      "com.gu" %% "editorial-permissions-client" % "5.0.0",
      "software.amazon.awssdk" % "sfn" % awsV2Version,
      "software.amazon.awssdk" % "ses" % awsV2Version,
      "software.amazon.awssdk" % "s3" % awsV2Version,
      "com.gu" %% "content-api-client-aws" % "1.0.1",
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % "test",
      "org.jsoup" % "jsoup" % jsoupVersion,
      "com.beachape" %% "enumeratum" % enumeratumVersion,
      "net.logstash.logback" % "logstash-logback-encoder" % "6.6"
    )
  )

lazy val normalisePackageName =
  taskKey[Unit]("Rename debian package name to be normalised")
lazy val app = (project in file("."))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(
    PlayScala,
    SbtWeb,
    JDebPackaging,
    SystemdPlugin,
    BuildInfoPlugin
  )
  .settings(
    commonSettings,
    name := "media-atom-maker",
    libraryDependencies ++= Seq(
      ehcache,
      "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
      "software.amazon.awssdk" % "sts" % awsV2Version,
      "software.amazon.awssdk" % "ec2" % awsV2Version,
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % "test",
      "org.mockito" %% "mockito-scala" % mockitoVersion % "test",
      "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion % "test"
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
    maintainer := "Digital CMS <digitalcms.dev@guardian.co.uk>",
    packageSummary := "media-atom-maker",
    packageDescription := """making media atoms""",
    pipelineStages := Seq(digest, gzip),
    normalisePackageName := {
      val targetDirectory = baseDirectory.value / "target"
      val debFile = (targetDirectory ** "*.deb").get().head
      val newFile =
        file(debFile.getParent) / ((Debian / packageName).value + ".deb")

      IO.move(debFile, newFile)
    }
  )

lazy val uploader = (project in file("uploader"))
  .dependsOn(common)
  .enablePlugins(JavaAppPackaging)
  .settings(
    commonSettings,
    name := "media-atom-uploader",
    libraryDependencies ++= Seq(
      "net.logstash.logback" % "logstash-logback-encoder" % logstashLogbackEncoderVersion,
      "com.amazonaws" % "aws-lambda-java-events" % awsLambdaEventsVersion
    ),
    Universal / topLevelDirectory := None,
    Universal / packageName := normalizedName.value,
    Compile / lambdas := Map(
      "GetChunkFromS3" -> LambdaConfig(
        description =
          "Checks to see if a chunk of video has been uploaded to S3"
      ),
      "UploadChunkToYouTube" -> LambdaConfig(
        description = "Uploads a chunk of video to YouTube"
      ),
      "MultipartCopyChunkInS3" -> LambdaConfig(
        description =
          "Uses multipart copy to combine all the chunks in S3 into a single key"
      ),
      "CompleteMultipartCopy" -> LambdaConfig(
        description =
          "Finishes the multipart copy and deletes the source chunks from S3"
      ),
      "SendToPluto" -> LambdaConfig(
        description = "Sends a complete video to Pluto for ingestion"
      ),
      "SendToTranscoderV2" -> LambdaConfig(
        description =
          "Sends a complete video to the AWS MediaConvert transcoder"
      ),
      "GetTranscodingProgressV2" -> LambdaConfig(
        description = "Polls the AWS MediaConvert transcoder"
      ),
      "AddAssetToAtom" -> LambdaConfig(
        description = "Adds the resulting asset to the atom"
      ),
      "AddUploadDataToCache" -> LambdaConfig(
        description =
          "Adds the upload information to a Dynamo table so it is preserved even if the pipeline changes"
      )
    ),
    Compile / resourceGenerators += compileTemplate.taskValue
  )

lazy val integrationTests = (project in file("integration-tests"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    commonSettings,
    name := "integration-tests",
    Test / logBuffered := false,
    Test / parallelExecution := false
  )

lazy val expirer = (project in file("expirer"))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(JavaAppPackaging)
  .settings(
    commonSettings,
    name := "media-atom-expirer",
    Universal / topLevelDirectory := None,
    Universal / packageName := normalizedName.value,
    libraryDependencies += "org.mockito" %% "mockito-scala" % mockitoVersion % "test",
  )

lazy val scheduler = (project in file("scheduler"))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(JavaAppPackaging)
  .settings(
    commonSettings,
    name := "media-atom-scheduler",
    Universal / topLevelDirectory := None,
    Universal / packageName := normalizedName.value
  )

lazy val root = (project in file("root"))
  .aggregate(common, app, uploader, expirer, scheduler)

addCommandAlias(
  "ciCommands",
  Seq(
    "scalafmtCheckAll",
    "clean",
    "compile",
    "test",
    "app/Debian/packageBin",
    "app/normalisePackageName",
    "uploader/Universal/packageBin",
    "expirer/Universal/packageBin",
    "scheduler/Universal/packageBin",
    "uploader/Compile/resourceManaged"
  ).mkString(";")
)
