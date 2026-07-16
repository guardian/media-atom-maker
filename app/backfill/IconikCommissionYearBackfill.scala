package backfill

import com.gu.media.aws.{AwsCredentials, CredentialsForAws}
import com.gu.media.iconik.{
  IconikCommission,
  IconikDynamoStoreWithParentIndex,
  IndexConfig
}
import com.typesafe.config.ConfigFactory
import util.AWSConfig

object IconikCommissionYearBackfill {

  private val DefaultYear = "2026"
  private val ValidStages = Set("CODE", "PROD")

  private val profileName = sys.env.getOrElse("AWS_PROFILE", "media-service")

  final case class CliArgs(stage: String, dryRun: Boolean)

  def main(args: Array[String]): Unit = {
    parseArgs(args.toList) match {
      case Left(error) =>
        Console.err.println(error)
        printUsage()
      case Right(cliArgs) =>
        run(cliArgs)
    }
  }

  private def run(cliArgs: CliArgs): Unit = {
    val config = ConfigFactory.load()
    val instanceCredentials = CredentialsForAws.profile(profileName)
    val credentials = AwsCredentials(
      instance = instanceCredentials,
      crossAccount = instanceCredentials,
      upload = instanceCredentials
    )
    val awsConfig = new AWSConfig(config, credentials)
    val tableName = iconikCommissionTableName(cliArgs.stage)

    println(s"Targeting stage=${cliArgs.stage} table=$tableName")

    val commissionStore =
      new IconikDynamoStoreWithParentIndex[IconikCommission](
        awsConfig,
        tableName,
        IndexConfig("working-group-index", "workingGroupId")
      )

    commissionStore.list match {
      case Left(err) =>
        Console.err.println(s"[error] failed to list commissions: $err")

      case Right(commissions) =>
        val missingYear = commissions.filter(_.year.isEmpty)

        missingYear.foreach { commission =>
          if (cliArgs.dryRun) {
            println(
              s"[dry-run] would update commissionId=${commission.id} year=$DefaultYear"
            )
          } else {
            commissionStore.upsert(commission.copy(year = Some(DefaultYear)))
            println(
              s"[updated] commissionId=${commission.id} year=$DefaultYear"
            )
          }
        }

        println()
        println("Backfill summary")
        println(s"  stage:                   ${cliArgs.stage}")
        println(s"  total commissions:       ${commissions.length}")
        println(s"  missing year (to fill):  ${missingYear.length}")
        println(
          s"  mode:                    ${if (cliArgs.dryRun) "dry-run"
            else "write"}"
        )
    }
  }

  private def parseArgs(args: List[String]): Either[String, CliArgs] = {
    val dryRun = args.contains("--dry-run")
    val rawStage = args.collectFirst {
      case arg if arg.startsWith("--stage=") => arg.stripPrefix("--stage=")
    }

    rawStage.map(_.trim.toUpperCase) match {
      case Some(stage) if ValidStages.contains(stage) =>
        Right(CliArgs(stage, dryRun))
      case Some(stage) =>
        Left(
          s"Invalid --stage=$stage. Must be one of: ${ValidStages.mkString(", ")}"
        )
      case None =>
        Left("Missing required --stage=CODE|PROD argument")
    }
  }

  private def iconikCommissionTableName(stage: String): String =
    s"media-atom-maker-$stage-iconik-commissions-table-v2"

  private def printUsage(): Unit = {
    println(
      s"""
         |Usage:
         |  sbt "app/runMain backfill.IconikCommissionYearBackfill --stage=CODE|PROD [--dry-run]"
         |
         |Notes:
         |  - --stage is required and must be CODE or PROD.
         |  - Uses AWS profile '$profileName' (override with AWS_PROFILE env var).
         |  - Use --dry-run first to preview updates before writing.
         |""".stripMargin
    )
  }
}
