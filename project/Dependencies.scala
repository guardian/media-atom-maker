import sbt._
import play.sbt.PlayImport

object Dependencies {
  val scroogeVersion = "4.12.0"
  val awsVersion = "1.11.48"
  val pandaVersion = "0.4.0"
  val mockitoVersion = "2.0.97-beta"
  val atomMakerVersion = "0.1.7"
  val slf4jVersion = "1.7.21"
  val typesafeConfigVersion = "1.3.0" // to match what we get from Play transitively

  val thrift = "org.apache.thrift" % "libthrift" % "0.9.3"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
  val cats = "org.typelevel" %% "cats-core" % "0.7.0" // for interacting with scanamo
  val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.0"
  val playJsonExtensions = "org.cvogt" %% "play-json-extensions" % "0.8.0"
  val okHttp = "com.squareup.okhttp" % "okhttp" % "2.4.0"
  val diff = "ai.x" %% "diff" % "1.2.0"
  val typesafeConfig = "com.typesafe" % "config" % "1.3.1"

  val contentAtomModel = "com.gu" %% "content-atom-model" %  "2.4.17"

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
  val mockito = "org.mockito" %  "mockito-core" % mockitoVersion % "test"
  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.5"   % "test"

  val awsSts = "com.amazonaws" % "aws-java-sdk-sts" % awsVersion
  val awsEc2 = "com.amazonaws" % "aws-java-sdk-ec2" % awsVersion
  val awsS3 = "com.amazonaws" % "aws-java-sdk-s3" % awsVersion
  val awsDynamo = "com.amazonaws" % "aws-java-sdk-dynamodb" % awsVersion
  val awsLambdaCore = "com.amazonaws" % "aws-lambda-java-core" % "1.1.0"
  val awsTranscoder = "com.amazonaws" % "aws-java-sdk-elastictranscoder" % awsVersion

  val logstashLogbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % "4.8"
  val kinesisLogbackAppender = "com.gu" % "kinesis-logback-appender" % "1.3.0"

  val panda = Seq(
    "com.gu" %% "pan-domain-auth-play_2-5" % pandaVersion,
    "com.gu" %% "pan-domain-auth-verification" % pandaVersion,
    "com.gu" %% "pan-domain-auth-core" % pandaVersion,
    "com.gu" %% "panda-hmac" % "1.1.0",
    PlayImport.ws
  )

  val scanamo = Seq(
    "com.gu" %% "scanamo" % "0.7.0",
    "com.gu" %% "scanamo-scrooge" % "0.1.3"
  )

  val scrooge = Seq(
    "com.twitter" %% "scrooge-core" % scroogeVersion,
    "com.twitter" %% "scrooge-serializer" % scroogeVersion
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

  val commonDependencies = googleApi ++ Seq(
    typesafeConfig, awsLambdaCore, awsS3, awsDynamo, playJsonExtensions, logstashLogbackEncoder, kinesisLogbackAppender, awsTranscoder
  )

  val appDependencies = panda ++ scanamo ++ scrooge ++ atomMaker ++ slf4j ++ Seq(
    PlayImport.cache, thrift, scalaLogging, cats, jacksonDatabind, okHttp, contentAtomModel, diff,
    awsSts, awsEc2, scalaTestPlusPlay, mockito, scalaXml, awsTranscoder
  )

  val uploaderDependencies = Seq(
    logstashLogbackEncoder, okHttp
  )

  val transcodeDependencies = Seq(awsLambdaCore, awsTranscoder, playJsonExtensions)
}
