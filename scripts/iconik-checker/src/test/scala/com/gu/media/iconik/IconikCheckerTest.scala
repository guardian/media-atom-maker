package com.gu.media.iconik

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scanamo.DynamoReadError
import play.api.libs.json.{JsObject, Json}
import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  StaticCredentialsProvider
}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class IconikCheckerTest extends AnyFlatSpec with Matchers {
  private implicit val executionContext: ExecutionContext =
    ExecutionContext.global

  private def withReportFile(test: Path => Unit): Unit = {
    val directory = Files.createTempDirectory("iconik-checker-")
    try test(directory.resolve("report.jsonl"))
    finally {
      List(
        "report.jsonl",
        "report.repairs.jsonl",
        "report.missing.jsonl",
        "report.name-mismatches.jsonl",
        "report.parent-mismatches.jsonl",
        "report.unrepairable.jsonl",
        "apply-plan.jsonl",
        "apply-plan.failed.jsonl"
      ).foreach(name => Files.deleteIfExists(directory.resolve(name)))
      Files.deleteIfExists(directory)
    }
  }

  private def reportedIds(path: Path): List[String] =
    Files.readAllLines(path, UTF_8).asScala.toList.map { line =>
      ((Json.parse(line) \ "entities")(0) \ "id").as[String]
    }

  "IconikChecker" should "require an explicit stage and parse report and plan paths" in {
    IconikChecker.parseOptions(List("CODE", "secret")).planOutput shouldBe
      Paths.get("iconik-checker-CODE.repairs.jsonl")
    IconikChecker
      .parseOptions(
        List(
          "CODE",
          "secret",
          "--output",
          "/tmp/report.jsonl",
          "--plan-output",
          "/tmp/plan.jsonl"
        )
      )
      .output shouldBe Paths.get("/tmp/report.jsonl")
    IconikChecker
      .parseOptions(
        List("CODE", "secret", "--plan-output", "/tmp/plan.jsonl")
      )
      .planOutput shouldBe Paths.get("/tmp/plan.jsonl")
    List(
      Nil,
      List("DEV", "secret"),
      List("CODE", "secret", "--unknown"),
      List("CODE", "secret", "--repair"),
      List("CODE", "secret", "--output"),
      List("CODE", "secret", "--output", "--repair")
    ).foreach { args =>
      intercept[IllegalArgumentException] {
        IconikChecker.parseOptions(args)
      }
    }
    IconikChecker.parseApplyOptions(
      List(
        "PROD",
        "/tmp/plan.jsonl",
        "--id",
        "project:one",
        "--id",
        "commission:two"
      )
    ) shouldBe IconikChecker.ApplyOptions(
      "PROD",
      Paths.get("/tmp/plan.jsonl"),
      Set("project:one", "commission:two"),
      applyAll = false
    )
    IconikChecker
      .parseApplyOptions(
        List("CODE", "/tmp/plan.jsonl", "--all")
      )
      .applyAll shouldBe true
    List(
      List("CODE", "/tmp/plan.jsonl"),
      List("CODE", "/tmp/plan.jsonl", "--all", "--id", "project:one"),
      List("CODE", "/tmp/plan.jsonl", "--id", "unknown:one")
    ).foreach { args =>
      intercept[IllegalArgumentException] {
        IconikChecker.parseApplyOptions(args)
      }
    }
  }

  it should "initialise standalone AWS settings without service configuration" in {
    val provider = StaticCredentialsProvider.create(
      AwsBasicCredentials.create("test-access-key", "test-secret-key")
    )
    val aws = IconikChecker.awsConfig(
      IconikChecker.parseOptions(List("CODE", "secret")),
      provider,
      Region.EU_WEST_1
    )
    aws.stage shouldBe "CODE"
    aws.iconikProjectTableName shouldBe "media-atom-maker-CODE-iconik-projects-table-v2"
    aws.iconikCommissionTableName shouldBe "media-atom-maker-CODE-iconik-commissions-table-v2"
    aws.iconikWorkingGroupTableName shouldBe "media-atom-maker-CODE-iconik-working-groups-table-v2"
    aws.credentials.instance.awsV2Creds shouldBe provider
  }

  it should "write JSON lines to a file without per-entity console noise and preserve partial results on failure" in {
    val groups =
      new IconikInMemoryStore[IconikWorkingGroup, DynamoReadError]()
    groups.upsert(IconikWorkingGroup("group-1", "First group"))
    groups.upsert(IconikWorkingGroup("group-2", "Second group"))
    var failSecond = false
    val service = new IconikValidationService(
      new IconikStore(
        new IconikInMemoryStoreWithParentIndex[
          IconikProject,
          DynamoReadError
        ](),
        new IconikInMemoryStoreWithParentIndex[
          IconikCommission,
          DynamoReadError
        ](),
        groups
      ),
      new IconikCollectionClient {
        override def getCollection(id: String, credentials: IconikCredentials) =
          Future.successful(
            if (failSecond && id == "group-2")
              Left(IconikRequestFailure("Iconik unavailable"))
            else Right(Some(IconikCollection("Title", None)))
          )
      },
      IconikCredentials("app", "token")
    )
    withReportFile { output =>
      val stdout = new ByteArrayOutputStream()
      val stderr = new ByteArrayOutputStream()
      def run(): Unit =
        IconikChecker.checkToFile(
          service,
          "CODE",
          output,
          output.resolveSibling("report.repairs.jsonl"),
          () => (),
          ansi = false
        )
      Console.withOut(stdout) {
        Console.withErr(stderr) {
          run()
        }
      }
      reportedIds(output) shouldBe List("group-1", "group-2")
      reportedIds(
        output.resolveSibling("report.name-mismatches.jsonl")
      ) shouldBe
        List("group-1", "group-2")
      val completePlan = Files
        .readAllLines(output.resolveSibling("report.repairs.jsonl"), UTF_8)
        .asScala
        .toList
      (Json.parse(completePlan.last) \ "type").as[String] shouldBe "complete"
      (Json.parse(completePlan.last) \ "changes").as[Int] shouldBe 2
      reportedIds(output.resolveSibling("report.missing.jsonl")) shouldBe empty
      reportedIds(
        output.resolveSibling("report.parent-mismatches.jsonl")
      ) shouldBe empty
      reportedIds(
        output.resolveSibling("report.unrepairable.jsonl")
      ) shouldBe empty
      stdout.toString(UTF_8) shouldBe ""
      stderr.toString(UTF_8) shouldBe
        "\rICONIK [--------------------] 0/2 (0%)\rICONIK [##########----------] 1/2 (50%)\rICONIK [####################] 2/2 (100%)\nChecked 2 Iconik objects.\n"

      stderr.reset()
      Console.withErr(stderr) {
        IconikChecker.checkToFile(
          service,
          "CODE",
          output,
          output.resolveSibling("report.repairs.jsonl"),
          () => (),
          ansi = true
        )
      }
      val colouredProgress = stderr.toString(UTF_8)
      colouredProgress should include(
        "\r\u001b[2K\u001b[1;36m\u001b[7m ICONIK \u001b[27m [--------------------] 0/2 (0%)\u001b[0m"
      )
      colouredProgress should include(
        "\r\u001b[2K\u001b[1;32m\u001b[7m ICONIK \u001b[27m [####################] 2/2 (100%)\u001b[0m"
      )
      reportedIds(output) shouldBe List("group-1", "group-2")

      failSecond = true
      stderr.reset()
      Console.withErr(stderr) {
        intercept[IllegalStateException](
          run()
        ).getMessage shouldBe "Iconik unavailable"
      }
      stderr.toString(UTF_8) shouldBe
        "\rICONIK [--------------------] 0/2 (0%)\rICONIK [##########----------] 1/2 (50%)\n"
      reportedIds(output) shouldBe List("group-1")
      reportedIds(
        output.resolveSibling("report.name-mismatches.jsonl")
      ) shouldBe
        List("group-1")
      val incompletePlan = Files
        .readAllLines(output.resolveSibling("report.repairs.jsonl"), UTF_8)
        .asScala
        .toList
      (Json.parse(incompletePlan.last) \ "type").as[String] shouldBe "change"
      var invoked = false
      intercept[IllegalArgumentException] {
        IconikChecker.applyPlan(
          IconikChecker.ApplyOptions(
            "CODE",
            output.resolveSibling("report.repairs.jsonl"),
            Set.empty,
            applyAll = true
          ),
          _ => {
            invoked = true
            0
          }
        )
      }
      invoked shouldBe false
    }
  }

  it should "split defects into separate files, write unrepairable entities and exclude healthy entities" in {
    val projects = new IconikInMemoryStoreWithParentIndex[
      IconikProject,
      DynamoReadError
    ]()
    projects.upsert(
      IconikProject(
        id = "unrepairable-project",
        title = "Project title",
        status = "active",
        workingGroupId = "group-1",
        commissionId = "commission-1",
        masterPlaceholderId = None,
        createdAt = None
      )
    )
    val commissions = new IconikInMemoryStoreWithParentIndex[
      IconikCommission,
      DynamoReadError
    ]()
    List("missing", "mismatched", "healthy").foreach { id =>
      commissions.upsert(IconikCommission("group-1", id, id))
    }
    val service = new IconikValidationService(
      new IconikStore(
        projects,
        commissions,
        new IconikInMemoryStore[IconikWorkingGroup, DynamoReadError]()
      ),
      new IconikCollectionClient {
        override def getCollection(id: String, credentials: IconikCredentials) =
          Future.successful(Right(id match {
            case "unrepairable-project" =>
              Some(
                IconikCollection("Project title", Some("missing-commission"))
              )
            case "missing" => None
            case "mismatched" =>
              Some(IconikCollection("New title", Some("group-2")))
            case "healthy" => Some(IconikCollection("healthy", Some("group-1")))
          }))
      },
      IconikCredentials("app", "token")
    )
    withReportFile { output =>
      IconikChecker.checkToFile(
        service,
        "CODE",
        output,
        output.resolveSibling("report.repairs.jsonl"),
        () => (),
        ansi = false
      )
      reportedIds(output).toSet shouldBe Set(
        "unrepairable-project",
        "missing",
        "mismatched",
        "healthy"
      )
      reportedIds(output.resolveSibling("report.missing.jsonl")) shouldBe List(
        "missing"
      )
      reportedIds(
        output.resolveSibling("report.name-mismatches.jsonl")
      ) shouldBe List("mismatched")
      reportedIds(
        output.resolveSibling("report.parent-mismatches.jsonl")
      ) shouldBe List("mismatched", "unrepairable-project")
      val unrepairableLines = Files
        .readAllLines(output.resolveSibling("report.unrepairable.jsonl"), UTF_8)
        .asScala
        .toList
        .map(Json.parse)
      unrepairableLines.map(json =>
        ((json \ "entities")(0) \ "id").as[String]
      ) shouldBe List("unrepairable-project")
      (unrepairableLines.head \ "reason").as[String] shouldBe
        "Iconik commission missing-commission is missing from Dynamo"
      val repairPlans = Files
        .readAllLines(
          output.resolveSibling("report.repairs.jsonl"),
          UTF_8
        )
        .asScala
        .toList
        .map(Json.parse)
        .filter(json => (json \ "type").as[String] == "change")
      repairPlans.map(json => (json \ "id").as[String]) shouldBe
        List("missing", "mismatched")
      (repairPlans.head \ "backup" \ "title").as[String] shouldBe "missing"
      (repairPlans.head \ "update" \ "title" \ "to").as[String] shouldBe
        "[deleted] missing"
    }
  }

  it should "show complete progress when there are no entities" in {
    val service = new IconikValidationService(
      new IconikStore(
        new IconikInMemoryStoreWithParentIndex[
          IconikProject,
          DynamoReadError
        ](),
        new IconikInMemoryStoreWithParentIndex[
          IconikCommission,
          DynamoReadError
        ](),
        new IconikInMemoryStore[IconikWorkingGroup, DynamoReadError]()
      ),
      new IconikCollectionClient {
        override def getCollection(id: String, credentials: IconikCredentials) =
          fail("No Iconik request should be made")
      },
      IconikCredentials("app", "token")
    )
    val stderr = new ByteArrayOutputStream()
    Console.withErr(stderr) {
      IconikChecker.check(
        service,
        "CODE",
        (_, _, _) => fail("No report should be emitted"),
        () => fail("No pause should be needed"),
        ansi = false
      )
    }
    stderr.toString(UTF_8) shouldBe
      "\rICONIK [####################] 0/0 (100%)\nChecked 0 Iconik objects.\n"
  }

  it should "apply selected plan IDs or an explicitly approved whole plan with conditional updates" in {
    withReportFile { output =>
      val planPath = output.resolveSibling("apply-plan.jsonl")
      val records = List(
        Json.obj(
          "type" -> "change",
          "version" -> 1,
          "stage" -> "CODE",
          "entityType" -> "working_group",
          "id" -> "group-1",
          "tableName" -> "media-atom-maker-CODE-iconik-working-groups-table-v2",
          "backup" -> Json.obj("id" -> "group-1", "title" -> "Old group"),
          "after" -> Json.obj("id" -> "group-1", "title" -> "New group"),
          "update" -> Json.obj(
            "title" -> Json.obj("from" -> "Old group", "to" -> "New group")
          )
        ),
        Json.obj(
          "type" -> "change",
          "version" -> 1,
          "stage" -> "CODE",
          "entityType" -> "commission",
          "id" -> "commission-1",
          "tableName" -> "media-atom-maker-CODE-iconik-commissions-table-v2",
          "backup" -> Json.obj(
            "id" -> "commission-1",
            "title" -> "Old commission",
            "workingGroupId" -> "group-1"
          ),
          "after" -> Json.obj(
            "id" -> "commission-1",
            "title" -> "New commission",
            "workingGroupId" -> "group-1"
          ),
          "update" -> Json.obj(
            "title" -> Json.obj(
              "from" -> "Old commission",
              "to" -> "New commission"
            )
          )
        )
      )
      Files.write(
        planPath,
        (records :+ Json.obj(
          "type" -> "complete",
          "version" -> 1,
          "stage" -> "CODE",
          "changes" -> records.size
        )).map(Json.stringify).mkString("\n").getBytes(UTF_8)
      )

      var selectedRequests = List.empty[UpdateItemRequest]
      IconikChecker.applyPlan(
        IconikChecker.ApplyOptions(
          "CODE",
          planPath,
          Set("working_group:group-1"),
          applyAll = false
        ),
        request => selectedRequests = selectedRequests :+ request
      )
      selectedRequests.size shouldBe 1
      val request = selectedRequests.head
      request.tableName() shouldBe
        "media-atom-maker-CODE-iconik-working-groups-table-v2"
      request.updateExpression() shouldBe
        "SET #field0 = :new0"
      request.conditionExpression() should include(
        "#field0 = :old0"
      )
      request.expressionAttributeValues().get(":old0").s() shouldBe
        "Old group"

      var allRequests = List.empty[UpdateItemRequest]
      IconikChecker.applyPlan(
        IconikChecker
          .ApplyOptions("CODE", planPath, Set.empty, applyAll = true),
        request => {
          allRequests = allRequests :+ request
          if (request.key().get("id").s() == "group-1")
            throw new RuntimeException("ConditionalCheckFailedException")
        }
      )
      allRequests.size shouldBe 2
      val failedLines = Files
        .readAllLines(output.resolveSibling("apply-plan.failed.jsonl"), UTF_8)
        .asScala
        .toList
        .map(Json.parse)
      failedLines.map(json => (json \ "id").as[String]) shouldBe List("group-1")
      (failedLines.head \ "error").as[String] shouldBe
        "ConditionalCheckFailedException"
    }
  }

  it should "check entities sequentially and emit repair plans without writing" in {
    val groups =
      new IconikInMemoryStore[IconikWorkingGroup, DynamoReadError]()
    groups.upsert(IconikWorkingGroup("group-1", "Old title"))
    groups.upsert(IconikWorkingGroup("group-2", "Other title"))
    val store = new IconikStore(
      new IconikInMemoryStoreWithParentIndex[IconikProject, DynamoReadError](),
      new IconikInMemoryStoreWithParentIndex[
        IconikCommission,
        DynamoReadError
      ](),
      groups
    )
    var events = List.empty[String]
    val client = new IconikCollectionClient {
      override def getCollection(id: String, credentials: IconikCredentials) = {
        events = events :+ id
        Future.successful(Right(Some(IconikCollection("New title", None))))
      }
    }
    val service = new IconikValidationService(
      store,
      client,
      IconikCredentials("app", "token")
    )
    var reports = List.empty[IconikValidationReport]
    var plans = List.empty[JsObject]
    IconikChecker.check(
      service,
      "CODE",
      (report, plan, _) => {
        reports = reports :+ report
        plan.foreach(value => plans = plans :+ value)
      },
      () => events = events :+ "pause"
    )
    events shouldBe List("group-1", "pause", "group-2", "pause")
    reports.map(_.summary.nameMismatches) shouldBe List(1, 1)
    plans.map(plan => (plan \ "id").as[String]) shouldBe
      List("group-1", "group-2")
    store
      .getWorkingGroup("group-1")
      .toOption
      .flatten
      .get
      .title shouldBe "Old title"
    store.getWorkingGroup("group-2").toOption.flatten.get.title shouldBe
      "Other title"
  }

  it should "plan commissions before projects and use the planned working group for unchanged and moved parents" in {
    val projects =
      new IconikInMemoryStoreWithParentIndex[IconikProject, DynamoReadError]()
    val commissions =
      new IconikInMemoryStoreWithParentIndex[
        IconikCommission,
        DynamoReadError
      ]()
    val storedProjects =
      List("commission-1", "other-commission").zipWithIndex.map {
        case (commissionId, index) =>
          IconikProject(
            id = s"project-$index",
            title = "Project",
            status = "active",
            workingGroupId = "group-1",
            commissionId = commissionId,
            masterPlaceholderId = None,
            createdAt = None
          )
      }
    storedProjects.foreach(projects.upsert)
    val commission = IconikCommission("group-1", "commission-1", "Commission")
    commissions.upsert(commission)
    val store = new IconikStore(
      projects,
      commissions,
      new IconikInMemoryStore[IconikWorkingGroup, DynamoReadError]()
    )
    var requestedIds = List.empty[String]
    val service = new IconikValidationService(
      store,
      new IconikCollectionClient {
        override def getCollection(
            id: String,
            credentials: IconikCredentials
        ) = {
          requestedIds = requestedIds :+ id
          Future.successful(
            Right(
              Some(
                if (id == commission.id)
                  IconikCollection(commission.title, Some("group-2"))
                else IconikCollection("Project", Some(commission.id))
              )
            )
          )
        }
      },
      IconikCredentials("app", "token")
    )
    var plans = List.empty[JsObject]
    IconikChecker.check(
      service,
      "CODE",
      (_, plan, _) => plan.foreach(value => plans = plans :+ value),
      () => (),
      ansi = false
    )

    requestedIds shouldBe List("commission-1", "project-0", "project-1")
    plans.map(plan => (plan \ "id").as[String]) shouldBe requestedIds
    (plans.head \ "after" \ "workingGroupId").as[String] shouldBe "group-2"
    plans.tail.foreach { plan =>
      (plan \ "after" \ "commissionId").as[String] shouldBe commission.id
      (plan \ "after" \ "workingGroupId").as[String] shouldBe "group-2"
    }
    storedProjects.foreach { project =>
      store.getProject(project.id) shouldBe Right(Some(project))
    }
    store.getCommission(commission.id) shouldBe Right(Some(commission))
  }

  it should "stop and fail when Iconik cannot be checked" in {
    val groups =
      new IconikInMemoryStore[IconikWorkingGroup, DynamoReadError]()
    groups.upsert(IconikWorkingGroup("group-1", "Title"))
    val service = new IconikValidationService(
      new IconikStore(
        new IconikInMemoryStoreWithParentIndex[
          IconikProject,
          DynamoReadError
        ](),
        new IconikInMemoryStoreWithParentIndex[
          IconikCommission,
          DynamoReadError
        ](),
        groups
      ),
      new IconikCollectionClient {
        override def getCollection(id: String, credentials: IconikCredentials) =
          Future.successful(Left(IconikRequestFailure("Iconik unavailable")))
      },
      IconikCredentials("app", "token")
    )
    val error = intercept[IllegalStateException] {
      IconikChecker.check(
        service,
        "CODE",
        (_, _, _) => fail("No report should be emitted"),
        () => fail("No further checks should run")
      )
    }
    error.getMessage shouldBe "Iconik unavailable"
  }
}
