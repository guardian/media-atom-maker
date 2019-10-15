import com.typesafe.sbt.SbtNativePackager.autoImport.maintainer
import com.typesafe.sbt.packager.archetypes.ServerLoader.Systemd
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.debianPackageDependencies
import sbt.Keys._
import StateMachine._
import play.sbt.PlayImport

val scroogeVersion = "4.12.0"
val awsVersion = "1.11.125"
val pandaVersion = "0.5.1"
val pandaHmacVersion = "1.2.0"
val atomMakerVersion = "1.2.2"
val slf4jVersion = "1.7.21"
val typesafeConfigVersion = "1.3.0" // to match what we get from Play transitively
val scanamoVersion = "1.0.0-M9" // to match what we get from atom-publisher-lib transitively

val scalaLoggingVersion = "3.4.0"
val jacksonDatabindVersion = "2.9.2"
val playJsonExtensionsVersion = "0.8.0"
val okHttpVersion = "2.4.0"
val diffVersion = "1.2.0"

val capiAwsVersion = "0.5"

val scalaTestVersion = "2.2.6"
val scalaTestPlusPlayVersion = "1.5.1"
val mockitoVersion = "2.0.97-beta"
val scalaXmlVersion = "1.0.5"
val scalaCheckVersion = "1.12.5" // to match ScalaTest version

val awsLambdaCoreVersion = "1.1.0"
val awsLambdaEventsVersion = "1.3.0"

val logstashLogbackEncoderVersion = "4.8"
val kinesisLogbackAppenderVersion = "1.4.2"

val permissionsClientVersion = "0.7"

val guavaVersion = "17.0"
val googleHttpJacksonVersion = "1.22.0"
val googleOauthVersion = "1.20.0"
val googleBugsVersion = "1.3.9"
val googleHttpVersion = "1.22.0"
val commonsLoggingVersion = "1.1.1"
val apacheHttpClientVersion = "4.0.1"
val apacheHttpCoreVersion = "4.0.1"

val googleApiClientVersion = "1.22.0"
val youTubeApiClientVersion = "v3-rev178-1.22.0"

val jsoupVersion = "1.8.3"

lazy val commonSettings = Seq(
  scalaVersion in ThisBuild := "2.11.8",
  organization in ThisBuild := "com.gu",

  resolvers ++= Seq("Guardian Bintray" at "https://dl.bintray.com/guardian/editorial-tools",
    "Sonatype OSS" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  ),

  // silly SBT command to work-around lack of support for root projects that are not in the "root" folder
  // https://github.com/sbt/sbt/issues/2405
  onLoad in Global := (onLoad in Global).value andThen (Command.process("project root", _))
)

lazy val common = (project in file("common"))
  .settings(commonSettings,
    name := "media-atom-common",
    unmanagedBase := baseDirectory.value / "common" / "lib",
    unmanagedJars in Compile += file("common/lib/google-api-services-youtubePartner-v1-rev20160726-java-1.22.0-sources.jar"),
    unmanagedJars in Compile += file("common/lib/google-api-services-youtubePartner-v1-rev20160726-java-1.22.0.jar"),
    libraryDependencies ++= Seq(
      "com.google.api-client" %  "google-api-client" % googleApiClientVersion,
      "com.google.apis" % "google-api-services-youtube" % youTubeApiClientVersion,
      "com.gu" %% "pan-domain-auth-play_2-5" % pandaVersion,
      "com.gu" %% "pan-domain-auth-verification" % pandaVersion,
      "com.gu" %% "pan-domain-auth-core" % pandaVersion,
      "com.gu" %% "panda-hmac" % pandaHmacVersion,
      PlayImport.ws,
      "com.gu" %% "atom-publisher-lib" % atomMakerVersion,
      "com.gu" %% "atom-publisher-lib" % atomMakerVersion % "test" classifier "tests",
      "com.gu" %% "atom-manager-play" % atomMakerVersion,
      "com.gu"  %% "atom-manager-play" % atomMakerVersion % "test" classifier "tests",
      "com.google.guava" % "guava-jdk5" % guavaVersion,
      "com.google.http-client" % "google-http-client-jackson2" % googleHttpJacksonVersion,
      "com.google.oauth-client" % "google-oauth-client-jetty" % googleOauthVersion,
      "com.google.code.findbugs" % "jsr305" % googleBugsVersion,
      "com.google.http-client" % "google-http-client" % googleHttpVersion,
      "commons-logging" % "commons-logging" % commonsLoggingVersion,
      "org.apache.httpcomponents" % "httpclient" % apacheHttpClientVersion,
      "org.apache.httpcomponents" % "httpcore" % apacheHttpCoreVersion,
      "com.typesafe" % "config" % typesafeConfigVersion,
      "com.amazonaws" % "aws-lambda-java-core" % awsLambdaCoreVersion,
      "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
      "com.amazonaws" % "aws-java-sdk-dynamodb" % awsVersion,
      "org.cvogt" %% "play-json-extensions" % playJsonExtensionsVersion,
      "net.logstash.logback" % "logstash-logback-encoder" % logstashLogbackEncoderVersion,
      "com.gu" % "kinesis-logback-appender" % kinesisLogbackAppenderVersion,
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
      "org.jsoup" % "jsoup" % jsoupVersion
    )
  )

lazy val app = (project in file("."))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(PlayScala, SbtWeb, BuildInfoPlugin, JDebPackaging)
  .settings(commonSettings,
    name := "media-atom-maker",
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.slf4j" % "jcl-over-slf4j" % slf4jVersion,
      PlayImport.cache,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion,
      "ai.x" %% "diff" % diffVersion,
      "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
      "com.amazonaws" % "aws-java-sdk-ec2" % awsVersion,
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % "test",
      "org.mockito" %  "mockito-core" % mockitoVersion % "test",
      "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion   % "test"
    ),

    aggregate in run := false,

    javaOptions in Universal ++= Seq(
      "-Dpidfile.path=/dev/null"
    ),

    buildInfoKeys := Seq[BuildInfoKey](
      name,
      BuildInfoKey.constant("gitCommitId", Option(System.getenv("BUILD_VCS_NUMBER")) getOrElse(try {
        "git rev-parse HEAD".!!.trim
      } catch {
        case e: Exception => "unknown"
      }))
    ),

    buildInfoPackage := "app",
    serverLoading in Debian := Systemd,

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
    topLevelDirectory in Universal := None,
    packageName in Universal := normalizedName.value,

    lambdas in Compile := Map(
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

    resourceGenerators in Compile += compileTemplate.taskValue
  )

lazy val integrationTests = (project in file("integration-tests"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(commonSettings,
    name := "integration-tests",
    logBuffered in Test := false,
    parallelExecution in Test := false
  )


lazy val expirer = (project in file("expirer"))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings,
    name := "media-atom-expirer",
    topLevelDirectory in Universal := None,
    packageName in Universal := normalizedName.value

  )

lazy val scheduler = (project in file("scheduler"))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings,
    name := "media-atom-scheduler",
    topLevelDirectory in Universal := None,
    packageName in Universal := normalizedName.value

  )

val jsTargetDir = "target/riffraff/packages"
val plutoMessageIngestion = "pluto-message-ingestion"

lazy val root = (project in file("root"))
  .aggregate(common, app, uploader, expirer, scheduler)
  .enablePlugins(RiffRaffArtifact)
  .settings(
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffManifestProjectName := "media-service:media-atom-maker",
    riffRaffArtifactResources := Seq(
      (packageBin in Debian in app).value -> s"${(name in app).value}/${(name in app).value}.deb",
      (packageBin in Universal in uploader).value -> s"media-atom-upload-actions/${(packageBin in Universal in uploader).value.getName}",
      (packageBin in Universal in expirer).value -> s"${(name in expirer).value}/${(packageBin in Universal in expirer).value.getName}",
      (packageBin in Universal in scheduler).value -> s"${(name in scheduler).value}/${(packageBin in Universal in scheduler).value.getName}",
      (baseDirectory in Global in app).value / s"$plutoMessageIngestion/$jsTargetDir/$plutoMessageIngestion/$plutoMessageIngestion.zip" -> s"$plutoMessageIngestion/$plutoMessageIngestion.zip",
      (baseDirectory in Global in app).value / "conf/riff-raff.yaml" -> "riff-raff.yaml",
      (resourceManaged in Compile in uploader).value / "media-atom-pipeline.yaml" -> "media-atom-pipeline-cloudformation/media-atom-pipeline.yaml"
    )
  )
