package backfill

import com.gu.media.aws.{AwsCredentials, CredentialsForAws}
import com.gu.media.iconik.{
  IconikCommission,
  IconikDynamoStoreWithParentIndex,
  IndexConfig
}
import com.typesafe.config.ConfigFactory
import org.scanamo.{DynamoReadError, Scanamo, Table}
import org.scanamo.syntax._
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

    // Scoped here rather than added to the shared IconikDynamoStore, since
    // partial-attribute updates are specific to this one-off migration.
    val scanamo: Scanamo = awsConfig.scanamo
    val commissionsTable: Table[IconikCommission] = Table[IconikCommission](
      tableName
    )

    def updateYear(
        commissionId: String,
        year: String
    ): Either[DynamoReadError, IconikCommission] =
      scanamo.exec(
        commissionsTable.update(
          "id" === commissionId,
          set("year" -> Some(year))
        )
      )

    commissionStore.list match {
      case Left(err) =>
        Console.err.println(s"[error] failed to list commissions: $err")

      case Right(commissions) =>
        // Treat a blank/whitespace-only string the same as a missing year
        def isMissingYear(commission: IconikCommission): Boolean =
          commission.year.forall(_.trim.isEmpty)

        val missingYear = commissions.filter(isMissingYear)
        val missingYearCount = missingYear.length

        missingYear.zipWithIndex.foreach { case (commission, index) =>
          val progress = s"(${index + 1} of $missingYearCount)"
          if (cliArgs.dryRun) {
            println(
              s"[dry-run] $progress would update commissionId=${commission.id} year=$DefaultYear"
            )
          } else {
            updateYear(commission.id, DefaultYear) match {
              case Right(_) =>
                println(
                  s"[updated] $progress commissionId=${commission.id} year=$DefaultYear"
                )
              case Left(err) =>
                Console.err.println(
                  s"[error] $progress failed to update commissionId=${commission.id}: $err"
                )
            }
          }
        }

        println()
        println("Backfill summary")
        println(s"  stage:                   ${cliArgs.stage}")
        println(s"  total commissions:       ${commissions.length}")
        println(s"  commissions missing year (to fill):  ${missingYearCount}")
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
