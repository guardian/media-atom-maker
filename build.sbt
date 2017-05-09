import com.typesafe.sbt.SbtNativePackager.autoImport.maintainer
import com.typesafe.sbt.packager.archetypes.ServerLoader.Systemd
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.debianPackageDependencies
import sbt.Keys._
import StateMachines._

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
    libraryDependencies ++= Dependencies.commonDependencies
  )

lazy val app = (project in file("."))
  .dependsOn(common)
  .enablePlugins(PlayScala, SbtWeb, BuildInfoPlugin, JDebPackaging)
  .settings(commonSettings,
    name := "media-atom-maker",
    libraryDependencies ++= Dependencies.appDependencies,

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
    packageDescription := """making media atoms"""
  )

lazy val uploader = (project in file("uploader"))
  .dependsOn(common)
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings,
    name := "media-atom-uploader",
    libraryDependencies ++= Dependencies.uploaderDependencies,

    topLevelDirectory in Universal := None,
    packageName in Universal := normalizedName.value,

    lambdas in Compile := Map(
      "GetChunkFromS3" -> LambdaConfig(
        description = "Checks to see if a chunk of video has been uploaded to S3"
      ),
      "UploadChunkToYouTube" -> LambdaConfig(
        description = "Uploads a chunk of video to YouTube"
      ),
      "CreateCompleteVideoInS3" -> LambdaConfig(
        description = "Uses multipart copy to combine all the chunks in S3 into a single key"
      ),
      "SendToPluto" -> LambdaConfig(
        description = "Sends a complete video to Pluto for ingestion"
      ),
      "SendToTranscoder" -> LambdaConfig(
        description = "Sends a complete video to the AWS transcoder"
      ),
      "CheckTranscodingComplete" -> LambdaConfig(
        description = "Polls the AWS transcoder"
      ),
      "AddAssetToAtom" -> LambdaConfig(
        description = "Adds the resulting asset to the atom"
      )
    ),

    resourceGenerators in Compile += compileTemplate.taskValue
  )

lazy val integrationTests = (project in file("integration-tests"))
  .dependsOn(common)
  .settings(commonSettings,
    name := "integration-tests",
    libraryDependencies ++= Dependencies.integrationTestDependencies,
    logBuffered in Test := false
  )


lazy val expirer = (project in file("expirer"))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings,
    name := "media-atom-expirer",
    libraryDependencies ++= Dependencies.expirerDependencies,

    topLevelDirectory in Universal := None,
    packageName in Universal := normalizedName.value

  )

val jsTargetDir = "target/riffraff/packages"
val plutoMessageIngestion = "pluto-message-ingestion"

lazy val root = (project in file("root"))
  .aggregate(common, app, uploader, expirer)
  .enablePlugins(RiffRaffArtifact)
  .settings(
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),
    riffRaffManifestProjectName := "media-service:media-atom-maker",
    riffRaffArtifactResources := Seq(
      (packageBin in Debian in app).value -> s"${(name in app).value}/${(name in app).value}.deb",
      (packageBin in Universal in uploader).value -> s"media-atom-upload-actions/${(packageBin in Universal in uploader).value.getName}",
      (packageBin in Universal in expirer).value -> s"${(name in expirer).value}/${(packageBin in Universal in expirer).value.getName}",
      (baseDirectory in Global in app).value / s"$plutoMessageIngestion/$jsTargetDir/$plutoMessageIngestion/$plutoMessageIngestion.zip" -> s"$plutoMessageIngestion/$plutoMessageIngestion.zip",
      (baseDirectory in Global in app).value / "conf/riff-raff.yaml" -> "riff-raff.yaml"
    )
  )
