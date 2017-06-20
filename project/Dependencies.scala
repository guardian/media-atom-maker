import sbt._
import play.sbt.PlayImport

object Dependencies {
  val scroogeVersion = "4.12.0"
  val awsVersion = "1.11.125"
  val pandaVersion = "0.4.0"
  val mockitoVersion = "2.0.97-beta"
  val atomMakerVersion = "1.0.3"
  val slf4jVersion = "1.7.21"
  val typesafeConfigVersion = "1.3.0" // to match what we get from Play transitively
  val scanamoVersion = "0.9.1" // to match what we get from atom-publisher-lib transitively

  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
  val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.0"
  val playJsonExtensions = "org.cvogt" %% "play-json-extensions" % "0.8.0"
  val okHttp = "com.squareup.okhttp" % "okhttp" % "2.4.0"
  val diff = "ai.x" %% "diff" % "1.2.0"
  val typesafeConfig = "com.typesafe" % "config" % "1.3.1"

  val scanamo = "com.gu" %% "scanamo" % scanamoVersion

  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test"
  val mockito = "org.mockito" %  "mockito-core" % mockitoVersion % "test"
  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.5"   % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.12.5" % "test" // to match ScalaTest version

  val awsSts = "com.amazonaws" % "aws-java-sdk-sts" % awsVersion
  val awsEc2 = "com.amazonaws" % "aws-java-sdk-ec2" % awsVersion
  val awsS3 = "com.amazonaws" % "aws-java-sdk-s3" % awsVersion
  val awsDynamo = "com.amazonaws" % "aws-java-sdk-dynamodb" % awsVersion
  val awsLambdaCore = "com.amazonaws" % "aws-lambda-java-core" % "1.1.0"
  val awsLambdaEvents = "com.amazonaws" % "aws-lambda-java-events" % "1.3.0"
  val awsTranscoder = "com.amazonaws" % "aws-java-sdk-elastictranscoder" % awsVersion
  val awsSNS = "com.amazonaws" % "aws-java-sdk-sns" % awsVersion
  val awsSQS = "com.amazonaws" % "aws-java-sdk-sqs" % awsVersion
  val awsStepFunctions = "com.amazonaws" % "aws-java-sdk-stepfunctions" % awsVersion
  val awsSES = "com.amazonaws" % "aws-java-sdk-ses" % awsVersion

  val logstashLogbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % "4.8"
  val kinesisLogbackAppender = "com.gu" % "kinesis-logback-appender" % "1.3.0"

  val permissionsClient = "com.gu" %% "editorial-permissions-client" % "0.7"

  val pandaHmacHeaders = "com.gu" %% "hmac-headers" % "1.1"

  val guava = "com.google.guava" % "guava-jdk5" % "17.0"
  val googleHttpJackson = "com.google.http-client" % "google-http-client-jackson2" % "1.22.0"
  val googleOauth = "com.google.oauth-client" % "google-oauth-client-jetty" % "1.20.0"
  val googleBugs = "com.google.code.findbugs" % "jsr305" % "1.3.9"
  val googleHttp = "com.google.http-client" % "google-http-client" % "1.22.0"
  val commonsLogging = "commons-logging" % "commons-logging" % "1.1.1"
  val apacheHttpClient = "org.apache.httpcomponents" % "httpclient" % "4.0.1"
  val apacheHttpCore = "org.apache.httpcomponents" % "httpcore" % "4.0.1"

  val jsoup = "org.jsoup" % "jsoup" % "1.8.3"

  val panda = Seq(
    "com.gu" %% "pan-domain-auth-play_2-5" % pandaVersion,
    "com.gu" %% "pan-domain-auth-verification" % pandaVersion,
    "com.gu" %% "pan-domain-auth-core" % pandaVersion,
    "com.gu" %% "panda-hmac" % "1.2.0",
    PlayImport.ws
  )

  val atomMaker = Seq(
    "com.gu" %% "atom-publisher-lib" % atomMakerVersion,
    "com.gu" %% "atom-publisher-lib" % atomMakerVersion % "test" classifier "tests",
    "com.gu" %% "atom-manager-play" % atomMakerVersion,
    "com.gu"  %% "atom-manager-play" % atomMakerVersion % "test" classifier "tests"
  )

  val slf4j = Seq(
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.slf4j" % "jcl-over-slf4j" % slf4jVersion
  )

  val googleApi = Seq(
    "com.google.api-client" %  "google-api-client" % "1.22.0",
    "com.google.apis" % "google-api-services-youtube" % "v3-rev178-1.22.0"
  )

  val partnerApiDepencies = Seq(guava, googleHttpJackson, googleOauth, googleBugs, googleHttp, commonsLogging,
    apacheHttpClient, apacheHttpCore)

  val commonDependencies = googleApi ++ Seq(
    typesafeConfig, awsLambdaCore, awsS3, awsDynamo, playJsonExtensions, logstashLogbackEncoder, kinesisLogbackAppender,
    awsTranscoder, scanamo, okHttp, scalaTest, scalaCheck, awsSQS, awsSNS, permissionsClient, awsStepFunctions, awsSES
  ) ++ partnerApiDepencies

  val appDependencies = panda ++ atomMaker ++ slf4j ++ Seq(
    PlayImport.cache, scalaLogging, jacksonDatabind, okHttp, diff,
    awsSts, awsEc2, scalaTestPlusPlay, mockito, scalaXml, awsTranscoder,
    awsSQS, awsSNS, awsS3, jsoup
  )

  val uploaderDependencies = Seq(
    logstashLogbackEncoder, awsLambdaEvents, okHttp, pandaHmacHeaders
  )

  val expirerDependencies = Seq(scalaTest)

  val integrationTestDependencies =
    panda ++ googleApi ++ Seq(
    scalaTest,
    okHttp,
    playJsonExtensions,
    typesafeConfig,
    awsS3
  )
}
