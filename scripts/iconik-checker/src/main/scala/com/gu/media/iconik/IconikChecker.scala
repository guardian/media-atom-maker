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
import play.api.libs.json._
import play.api.libs.ws.ahc.AhcWSClient
import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProvider,
  DefaultCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeValue,
  ReturnValue,
  UpdateItemRequest
}
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

import java.io.BufferedWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, Paths}
import scala.annotation.tailrec
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.Using
import scala.util.control.NonFatal

object IconikChecker {
  private val ansiEnabled =
    !sys.env.contains("NO_COLOR") && !sys.env.get("TERM").contains("dumb")

  private val usage =
    "usage: iconikChecker/runMain com.gu.media.iconik.IconikChecker CODE|PROD <secret-id> [--output <file>] [--plan-output <file>]\n" +
      "   or: iconikChecker/runMain com.gu.media.iconik.IconikChecker apply CODE|PROD <plan.jsonl> (--id <entity-type:id>... | --all)"

  case class Options(
      stage: String,
      secretId: String,
      output: Path,
      planOutput: Path
  )

  case class ApplyOptions(
      stage: String,
      plan: Path,
      ids: Set[String],
      applyAll: Boolean
  )

  private case class PlannedChange(
      stage: String,
      entityType: String,
      id: String,
      tableName: String,
      backup: JsObject,
      after: JsObject,
      updates: JsObject
  ) {
    def selector: String = s"$entityType:$id"

    def toJson: JsObject =
      Json.obj(
        "version" -> 1,
        "type" -> "change",
        "stage" -> stage,
        "entityType" -> entityType,
        "id" -> id,
        "tableName" -> tableName,
        "backup" -> backup,
        "after" -> after,
        "update" -> updates
      )
  }

  def parseOptions(args: List[String]): Options = {
    @tailrec
    def parseFlags(
        flags: List[String],
        output: Path,
        planOutput: Path
    ): (Path, Path) =
      flags match {
        case Nil => (output, planOutput)
        case "--output" :: path :: rest
            if path.nonEmpty && !path.startsWith("--") =>
          parseFlags(rest, Paths.get(path), planOutput)
        case "--plan-output" :: path :: rest
            if path.nonEmpty && !path.startsWith("--") =>
          parseFlags(rest, output, Paths.get(path))
        case _ => throw new IllegalArgumentException(usage)
      }

    args match {
      case stage :: secretId :: flags
          if validStage(stage) && secretId.nonEmpty =>
        val defaultOutput = Paths.get(s"iconik-checker-$stage.jsonl")
        val (output, planOutput) = parseFlags(
          flags,
          defaultOutput,
          Paths.get(s"iconik-checker-$stage.repairs.jsonl")
        )
        val reportPaths =
          Set(
            output.toAbsolutePath.normalize(),
            defectPath(output, "missing").toAbsolutePath.normalize(),
            defectPath(output, "name-mismatches").toAbsolutePath.normalize(),
            defectPath(output, "parent-mismatches").toAbsolutePath.normalize(),
            defectPath(output, "unrepairable").toAbsolutePath.normalize()
          )
        require(
          !reportPaths.contains(planOutput.toAbsolutePath.normalize()),
          usage
        )
        Options(stage, secretId, output, planOutput)
      case _ => throw new IllegalArgumentException(usage)
    }
  }

  def parseApplyOptions(args: List[String]): ApplyOptions = {
    @tailrec
    def parseFlags(
        flags: List[String],
        ids: Set[String],
        applyAll: Boolean
    ): (Set[String], Boolean) =
      flags match {
        case Nil => (ids, applyAll)
        case "--id" :: selector :: rest
            if selector.nonEmpty && !selector.startsWith("--") =>
          parseSelector(selector)
          require(!ids.contains(selector), s"Duplicate ID selector: $selector")
          parseFlags(rest, ids + selector, applyAll)
        case "--all" :: rest if !applyAll =>
          parseFlags(rest, ids, applyAll = true)
        case _ => throw new IllegalArgumentException(usage)
      }

    args match {
      case stage :: plan :: flags if validStage(stage) && plan.nonEmpty =>
        val (ids, applyAll) = parseFlags(flags, Set.empty, applyAll = false)
        require(ids.nonEmpty ^ applyAll, usage)
        ApplyOptions(stage, Paths.get(plan), ids, applyAll)
      case _ => throw new IllegalArgumentException(usage)
    }
  }

  def main(args: Array[String]): Unit =
    args.toList match {
      case "apply" :: rest =>
        runApply(parseApplyOptions(rest))
      case _ =>
        runCheck(parseOptions(args.toList))
    }

  private def runCheck(options: Options): Unit = {
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
        Console.err.println(s"Checking ${options.stage} Iconik tables")
        checkToFile(
          service,
          options.stage,
          options.output,
          options.planOutput,
          () => Thread.sleep(1000)
        )
        Console.err.println(
          s"Reports written to ${options.output.toAbsolutePath}; repair plan written to ${options.planOutput.toAbsolutePath}"
        )
      }.get
    } finally {
      Await.result(system.terminate(), 30.seconds)
    }
  }

  private[iconik] def checkToFile(
      service: IconikValidationService,
      stage: String,
      output: Path,
      planOutput: Path,
      pause: () => Unit,
      ansi: Boolean = ansiEnabled
  ): Unit =
    Using.Manager { use =>
      val writer = use(Files.newBufferedWriter(output, UTF_8))
      val planWriter = use(Files.newBufferedWriter(planOutput, UTF_8))
      def defectWriter(defect: String): BufferedWriter = {
        use(Files.newBufferedWriter(defectPath(output, defect), UTF_8))
      }
      val missing = defectWriter("missing")
      val nameMismatches = defectWriter("name-mismatches")
      val parentMismatches = defectWriter("parent-mismatches")
      val unrepairable = defectWriter("unrepairable")
      var planCount = 0

      def write(destination: BufferedWriter, line: String): Unit = {
        destination.write(line)
        destination.newLine()
        destination.flush()
      }

      check(
        service,
        stage,
        (report, plan, unrepairableRecord) => {
          val line = Json.stringify(report.toJson)
          write(writer, line)
          if (report.summary.missing > 0) write(missing, line)
          if (report.summary.nameMismatches > 0) write(nameMismatches, line)
          if (report.summary.parentMismatches > 0) write(parentMismatches, line)
          unrepairableRecord.foreach(record =>
            write(unrepairable, Json.stringify(record))
          )
          plan.foreach { change =>
            write(planWriter, Json.stringify(change))
            planCount += 1
          }
        },
        pause,
        ansi
      )
      write(
        planWriter,
        Json.stringify(
          Json.obj(
            "type" -> "complete",
            "version" -> 1,
            "stage" -> stage,
            "changes" -> planCount
          )
        )
      )
    }.get

  private def defectPath(output: Path, defect: String): Path = {
    val stem = output.getFileName.toString.stripSuffix(".jsonl")
    output.resolveSibling(s"$stem.$defect.jsonl")
  }

  private[iconik] def awsConfig(
      requestedStage: String,
      credentialsProvider: AwsCredentialsProvider,
      awsRegion: Region
  ): Settings with AwsAccess with DynamoAccess = {
    val credentialsForAws = CredentialsForAws(credentialsProvider)
    val aws = new Settings with AwsAccess with DynamoAccess {
      override val config = ConfigFactory.empty()
      override def readTag(tag: String): Option[String] =
        if (tag == "Stage") Some(requestedStage) else None
      override val credentials: AwsCredentials =
        AwsCredentials(credentialsForAws, credentialsForAws, credentialsForAws)
      override def region: Region = awsRegion
    }
    require(
      aws.stage == requestedStage,
      "STAGE environment variable must match the requested stage"
    )
    aws
  }

  private[iconik] def awsConfig(
      options: Options,
      credentialsProvider: AwsCredentialsProvider,
      awsRegion: Region
  ): Settings with AwsAccess with DynamoAccess =
    awsConfig(options.stage, credentialsProvider, awsRegion)

  private[iconik] def check(
      service: IconikValidationService,
      stage: String,
      emit: (
          IconikValidationReport,
          Option[JsObject],
          Option[JsObject]
      ) => Unit,
      pause: () => Unit,
      ansi: Boolean = ansiEnabled
  ): Unit = {
    val entityIds = Await
      .result(service.listEntityIds(), 10.minutes)
      .fold(error => throw new IllegalStateException(error.message), identity)
    val total = entityIds.size
    var plannedCommissionWorkingGroups = Map.empty[String, String]

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
        val (report, decision) = Await
          .result(
            service.validateWithPlan(
              entityType,
              entity.id,
              plannedCommissionWorkingGroups
            ),
            2.minutes
          )
          .fold(
            error =>
              throw new IllegalStateException(
                error.message,
                error.cause.orNull
              ),
            identity
          )
        val (planJson, unrepairableJson) = decision match {
          case Some(plan: IconikRepairPlan) =>
            if (entityType == IconikEntityType.Commission) {
              plannedCommissionWorkingGroups +=
                plan.id -> (plan.after \ "workingGroupId").as[String]
            }
            (Some(plannedChange(stage, plan).toJson), None)
          case Some(unrepairable: IconikUnrepairable) =>
            (None, Some(unrepairable.toJson(report)))
          case None => (None, None)
        }
        emit(report, planJson, unrepairableJson)
        showProgress(index + 1)
        pause()
      }
    } finally Console.err.println()
    Console.err.println(s"Checked $total Iconik objects.")
  }

  private def plannedChange(
      stage: String,
      plan: IconikRepairPlan
  ): PlannedChange = {
    val table = tableName(stage, plan.entityType)
    require(
      (plan.before \ "id").asOpt[String].contains(plan.id) &&
        (plan.after \ "id").asOpt[String].contains(plan.id),
      s"Repair plan ID does not match item ${plan.id}"
    )
    val updates = plan.after.fields.toList.flatMap { case (name, value) =>
      plan.before.value.get(name) match {
        case Some(previous) if previous != value =>
          Some(name -> Json.obj("from" -> previous, "to" -> value))
        case Some(_) => None
        case None =>
          throw new IllegalArgumentException(
            s"Repair plan for ${plan.id} adds unsupported field $name"
          )
      }
    }
    require(updates.nonEmpty, s"Repair plan for ${plan.id} has no changes")
    PlannedChange(
      stage,
      plan.entityType,
      plan.id,
      table,
      plan.before,
      plan.after,
      JsObject(updates)
    )
  }

  private def tableName(stage: String, entityType: String): String = {
    val table = entityType match {
      case "project"       => "iconik-projects"
      case "commission"    => "iconik-commissions"
      case "working_group" => "iconik-working-groups"
      case _ =>
        throw new IllegalArgumentException(s"Unknown entity type: $entityType")
    }
    s"media-atom-maker-$stage-$table-table-v2"
  }

  private def runApply(options: ApplyOptions): Unit =
    Using.Manager { use =>
      val credentialsProvider =
        use(DefaultCredentialsProvider.builder().build())
      val aws = awsConfig(
        options.stage,
        credentialsProvider,
        Region.of(sys.env.getOrElse("AWS_REGION", "eu-west-1"))
      )
      val dynamoDb = use(aws.dynamoDbSdkV2)
      use(aws.dynamoDbAsyncSdkV2)
      applyPlan(options, request => dynamoDb.updateItem(request))
    }.get

  private[iconik] def applyPlan(
      options: ApplyOptions,
      updateItem: UpdateItemRequest => Any
  ): Unit = {
    require(options.applyAll ^ options.ids.nonEmpty, usage)
    requireStage(options.stage)
    val lines = Files.readAllLines(options.plan, UTF_8).asScala.toList
    require(lines.nonEmpty, "Repair plan is empty or incomplete")
    val completion = Json.parse(lines.last).as[JsObject]
    require(
      (completion \ "type").as[String] == "complete",
      "Repair plan is incomplete"
    )
    require((completion \ "version").as[Int] == 1, "Unsupported plan version")
    require(
      (completion \ "stage").as[String] == options.stage,
      s"Plan stage does not match ${options.stage}"
    )
    val changeLines = lines.dropRight(1)
    require(
      (completion \ "changes").as[Int] == changeLines.size,
      "Repair plan count does not match its completion record"
    )
    val changes = changeLines.zipWithIndex.collect {
      case (line, index) if line.trim.nonEmpty =>
        parsePlannedChange(line, index + 1, options.stage)
    }
    require(
      changes.size == changeLines.size,
      "Repair plan contains blank lines"
    )
    val selectors = changes.map(_.selector)
    require(
      selectors.distinct.size == selectors.size,
      "Repair plan contains duplicate entity IDs"
    )
    if (!options.applyAll) {
      val notFound = options.ids -- selectors.toSet
      require(
        notFound.isEmpty,
        s"IDs not found in repair plan: ${notFound.toList.sorted.mkString(", ")}"
      )
    }
    val selected =
      if (options.applyAll) changes
      else changes.filter(change => options.ids.contains(change.selector))

    val failedPath = defectPath(options.plan, "failed")
    Using.resource(Files.newBufferedWriter(failedPath, UTF_8)) { failedWriter =>
      var appliedCount = 0
      var failedCount = 0
      selected.zipWithIndex.foreach { case (change, index) =>
        Console.err.println(
          s"Applying ${change.selector} (${index + 1}/${selected.size})"
        )
        try {
          updateItem(updateRequest(change))
          appliedCount += 1
        } catch {
          case NonFatal(error) =>
            failedCount += 1
            val message = Option(error.getMessage).getOrElse(error.toString)
            Console.err.println(s"Failed applying ${change.selector}: $message")
            failedWriter.write(
              Json.stringify(change.toJson + ("error" -> JsString(message)))
            )
            failedWriter.newLine()
            failedWriter.flush()
        }
      }
      Console.err.println(
        s"Applied $appliedCount repair(s), $failedCount failed."
      )
    }
  }

  private def parsePlannedChange(
      line: String,
      lineNumber: Int,
      requestedStage: String
  ): PlannedChange =
    try {
      val json = Json.parse(line).as[JsObject]
      require(
        (json \ "type").as[String] == "change",
        "Expected a change record"
      )
      val version = (json \ "version").as[Int]
      require(version == 1, s"Unsupported plan version: $version")
      val stage = (json \ "stage").as[String]
      require(
        stage == requestedStage,
        s"Plan stage $stage does not match $requestedStage"
      )
      val entityType = (json \ "entityType").as[String]
      val id = (json \ "id").as[String]
      require(id.nonEmpty, "Plan ID must not be empty")
      val table = (json \ "tableName").as[String]
      require(
        table == tableName(stage, entityType),
        s"Unexpected table $table for $entityType"
      )
      val backup = (json \ "backup").as[JsObject]
      val after = (json \ "after").as[JsObject]
      val updates = (json \ "update").as[JsObject]
      require(
        (backup \ "id").as[String] == id,
        "Backup ID does not match plan ID"
      )
      require(
        (after \ "id").as[String] == id,
        "Updated ID does not match plan ID"
      )
      validateUpdates(entityType, backup, after, updates)
      PlannedChange(stage, entityType, id, table, backup, after, updates)
    } catch {
      case error: Exception =>
        throw new IllegalArgumentException(
          s"Invalid repair plan line $lineNumber: ${error.getMessage}",
          error
        )
    }

  private def validateUpdates(
      entityType: String,
      backup: JsObject,
      after: JsObject,
      updates: JsObject
  ): Unit = {
    val allowedFields = entityType match {
      case "project"       => Set("title", "commissionId", "workingGroupId")
      case "commission"    => Set("title", "workingGroupId")
      case "working_group" => Set("title")
      case _ =>
        throw new IllegalArgumentException(s"Unknown entity type: $entityType")
    }
    require(updates.keys.nonEmpty, "Repair plan has no updates")
    updates.fields.foreach { case (field, values) =>
      require(allowedFields.contains(field), s"Unsupported field: $field")
      val update = values.as[JsObject]
      val from = (update \ "from").as[String]
      val to = (update \ "to").as[String]
      require(
        (backup \ field).as[String] == from,
        s"Backup value mismatch for $field"
      )
      require(
        (after \ field).as[String] == to,
        s"Updated value mismatch for $field"
      )
      require(from != to, s"Update for $field does not change its value")
    }
    val changedFields = (backup.keys ++ after.keys).toSet.filter { field =>
      backup.value.get(field) != after.value.get(field)
    }
    require(
      changedFields == updates.keys,
      "Repair plan updates do not match the backup-to-after differences"
    )
  }

  private def updateRequest(change: PlannedChange): UpdateItemRequest = {
    val fields = change.updates.fields.sortBy(_._1)
    val names = fields.zipWithIndex.map { case ((field, _), index) =>
      s"#field$index" -> field
    }
    val values = fields.zipWithIndex.flatMap { case ((_, update), index) =>
      val from = (update \ "from").as[String]
      val to = (update \ "to").as[String]
      Seq(
        s":old$index" -> AttributeValue.builder().s(from).build(),
        s":new$index" -> AttributeValue.builder().s(to).build()
      )
    }
    val setExpressions =
      fields.indices.map(index => s"#field$index = :new$index")
    val conditions = fields.indices.map(index => s"#field$index = :old$index")
    UpdateItemRequest
      .builder()
      .tableName(change.tableName)
      .key(
        Map("id" -> AttributeValue.builder().s(change.id).build()).asJava
      )
      .updateExpression(s"SET ${setExpressions.mkString(", ")}")
      .conditionExpression(
        s"attribute_exists(#id) AND ${conditions.mkString(" AND ")}"
      )
      .expressionAttributeNames((Map("#id" -> "id") ++ names).asJava)
      .expressionAttributeValues(values.toMap.asJava)
      .returnValues(ReturnValue.ALL_NEW)
      .build()
  }

  private def parseSelector(selector: String): (String, String) = {
    val separator = selector.indexOf(':')
    require(separator > 0 && separator < selector.length - 1, usage)
    val entityType = selector.substring(0, separator)
    require(
      Set("project", "commission", "working_group").contains(entityType),
      s"Unknown entity type in selector: $selector"
    )
    (entityType, selector.substring(separator + 1))
  }

  private def validStage(stage: String): Boolean =
    Set("CODE", "PROD").contains(stage)

  private def requireStage(stage: String): Unit =
    sys.env
      .get("STAGE")
      .foreach(actual =>
        require(
          actual == stage,
          "STAGE environment variable must match the requested stage"
        )
      )
}
