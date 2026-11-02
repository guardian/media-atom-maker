package com.gu.media.iconik

import com.gu.media.Settings
import com.gu.media.aws.{
  AwsAccess,
  AwsCredentials,
  CredentialsForAws,
  DynamoAccess
}
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.SystemMaterializer
import play.api.libs.json.Json
import play.api.libs.ws.ahc.AhcWSClient
import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProvider,
  DefaultCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

import java.io.BufferedWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, Paths}
import scala.annotation.tailrec
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Using

object IconikChecker {
  private val ansiEnabled =
    !sys.env.contains("NO_COLOR") && !sys.env.get("TERM").contains("dumb")

  private val usage =
    "usage: iconikChecker/runMain com.gu.media.iconik.IconikChecker CODE|PROD <secret-id> [--repair] [--output <file>]"

  case class Options(
      stage: String,
      secretId: String,
      repair: Boolean,
      output: Path
  )

  def parseOptions(args: List[String]): Options = {
    @tailrec
    def parseFlags(flags: List[String], options: Options): Options =
      flags match {
        case Nil => options
        case "--repair" :: rest =>
          parseFlags(rest, options.copy(repair = true))
        case "--output" :: path :: rest
            if path.nonEmpty && !path.startsWith("--") =>
          parseFlags(rest, options.copy(output = Paths.get(path)))
        case _ => throw new IllegalArgumentException(usage)
      }

    args match {
      case stage :: secretId :: flags
          if Set("CODE", "PROD").contains(stage) && secretId.nonEmpty =>
        parseFlags(
          flags,
          Options(
            stage,
            secretId,
            repair = false,
            Paths.get(s"iconik-checker-$stage.jsonl")
          )
        )
      case _ => throw new IllegalArgumentException(usage)
    }
  }

  def main(args: Array[String]): Unit = {
    val options = parseOptions(args.toList)
    implicit val system: ActorSystem = ActorSystem("iconik-checker")
    implicit val materializer: org.apache.pekko.stream.Materializer =
      SystemMaterializer(system).materializer
    implicit val executionContext: ExecutionContext = system.dispatcher

    try {
      Using.Manager { use =>
        val credentialsProvider =
          use(DefaultCredentialsProvider.builder().build())
        val aws = awsConfig(
          options,
          credentialsProvider,
          Region.of(sys.env.getOrElse("AWS_REGION", "eu-west-1"))
        )
        use(aws.dynamoDbSdkV2)
        use(aws.dynamoDbAsyncSdkV2)
        val secrets = use(
          SecretsManagerClient
            .builder()
            .credentialsProvider(credentialsProvider)
            .region(aws.region)
            .build()
        )
        val secret = secrets
          .getSecretValue(
            GetSecretValueRequest.builder().secretId(options.secretId).build()
          )
          .secretString()
        val wsClient = use(AhcWSClient())
        val store = new IconikStore(
          new IconikDynamoStoreWithParentIndex[IconikProject](
            aws,
            aws.iconikProjectTableName,
            IndexConfig("commission-index", "commissionId")
          ),
          new IconikDynamoStoreWithParentIndex[IconikCommission](
            aws,
            aws.iconikCommissionTableName,
            IndexConfig("working-group-index", "workingGroupId")
          ),
          new IconikDynamoStore[IconikWorkingGroup](
            aws,
            aws.iconikWorkingGroupTableName
          )
        )
        val service = new IconikValidationService(
          store,
          new IconikApiClient(wsClient),
          IconikCredentials.fromSecret(secret)
        )
        Console.err.println(
          s"Checking ${options.stage} Iconik tables; repair=${options.repair}"
        )
        checkToFile(
          service,
          options.repair,
          options.output,
          () => Thread.sleep(1000)
        )
        Console.err.println(
          s"Reports written to ${options.output.toAbsolutePath} and its defect files"
        )
      }.get
    } finally {
      Await.result(system.terminate(), 30.seconds)
    }
  }

  private[iconik] def checkToFile(
      service: IconikValidationService,
      repair: Boolean,
      output: Path,
      pause: () => Unit,
      ansi: Boolean = ansiEnabled
  ): Unit =
    Using.Manager { use =>
      val writer = use(Files.newBufferedWriter(output, UTF_8))
      def defectWriter(defect: String): BufferedWriter = {
        val stem = output.getFileName.toString.stripSuffix(".jsonl")
        val path = output.resolveSibling(s"$stem.$defect.jsonl")
        use(Files.newBufferedWriter(path, UTF_8))
      }
      val missing = defectWriter("missing")
      val nameMismatches = defectWriter("name-mismatches")
      val parentMismatches = defectWriter("parent-mismatches")

      def write(destination: BufferedWriter, line: String): Unit = {
        destination.write(line)
        destination.newLine()
        destination.flush()
      }

      check(
        service,
        repair,
        report => {
          val line = Json.stringify(report.toJson)
          write(writer, line)
          if (report.summary.missing > 0) write(missing, line)
          if (report.summary.nameMismatches > 0) write(nameMismatches, line)
          if (report.summary.parentMismatches > 0) write(parentMismatches, line)
        },
        pause,
        ansi
      )
    }.get

  private[iconik] def awsConfig(
      options: Options,
      credentialsProvider: AwsCredentialsProvider,
      awsRegion: Region
  ): Settings with AwsAccess with DynamoAccess = {
    val credentialsForAws = CredentialsForAws(credentialsProvider)
    val aws = new Settings with AwsAccess with DynamoAccess {
      override val config = ConfigFactory.empty()
      override def readTag(tag: String): Option[String] =
        if (tag == "Stage") Some(options.stage) else None
      override val credentials: AwsCredentials =
        AwsCredentials(credentialsForAws, credentialsForAws, credentialsForAws)
      override def region: Region = awsRegion
    }
    require(
      aws.stage == options.stage,
      "STAGE environment variable must match the requested stage"
    )
    aws
  }

  private[iconik] def check(
      service: IconikValidationService,
      repair: Boolean,
      emit: IconikValidationReport => Unit,
      pause: () => Unit,
      ansi: Boolean = ansiEnabled
  ): Unit = {
    val entityIds = Await
      .result(service.listEntityIds(), 10.minutes)
      .fold(error => throw new IllegalStateException(error.message), identity)
    val total = entityIds.size

    def showProgress(completed: Int): Unit = {
      val percent =
        if (total == 0) 100 else completed.toLong * 100 / total
      val filled = (percent / 5).toInt
      val bar = "#" * filled + "-" * (20 - filled)
      val progress = s"[$bar] $completed/$total ($percent%)"
      val line =
        if (ansi) {
          val colour = if (percent == 100) "\u001b[1;32m" else "\u001b[1;36m"
          s"\r\u001b[2K$colour\u001b[7m ICONIK \u001b[27m $progress\u001b[0m"
        } else s"\rICONIK $progress"
      Console.err.print(line)
      Console.err.flush()
    }

    showProgress(0)
    try {
      entityIds.zipWithIndex.foreach { case (entity, index) =>
        val entityType = IconikEntityType
          .fromString(entity.entityType)
          .getOrElse(
            throw new IllegalArgumentException(
              s"Unknown Iconik entity type: ${entity.entityType}"
            )
          )
        val report = Await
          .result(service.validate(entityType, entity.id, repair), 2.minutes)
          .fold(
            error =>
              throw new IllegalStateException(
                error.message,
                error.cause.orNull
              ),
            identity
          )
        emit(report)
        showProgress(index + 1)
        pause()
      }
    } finally Console.err.println()
    Console.err.println(s"Checked $total Iconik objects.")
  }
}
