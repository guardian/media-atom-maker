package com.gu.media.iconik

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scanamo.DynamoReadError
import play.api.libs.json.Json
import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  StaticCredentialsProvider
}
import software.amazon.awssdk.regions.Region

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
        "report.missing.jsonl",
        "report.name-mismatches.jsonl",
        "report.parent-mismatches.jsonl"
      ).foreach(name => Files.deleteIfExists(directory.resolve(name)))
      Files.deleteIfExists(directory)
    }
  }

  private def reportedIds(path: Path): List[String] =
    Files.readAllLines(path, UTF_8).asScala.toList.map { line =>
      ((Json.parse(line) \ "entities")(0) \ "id").as[String]
    }

  "IconikChecker" should "require an explicit stage and opt in to repairs" in {
    IconikChecker.parseOptions(List("CODE", "secret")).repair shouldBe false
    IconikChecker
      .parseOptions(List("PROD", "secret", "--repair"))
      .repair shouldBe true
    IconikChecker
      .parseOptions(
        List("CODE", "secret", "--output", "/tmp/report.jsonl", "--repair")
      )
      .output shouldBe Paths.get("/tmp/report.jsonl")
    List(
      Nil,
      List("DEV", "secret"),
      List("CODE", "secret", "--unknown"),
      List("CODE", "secret", "--repair", "extra"),
      List("CODE", "secret", "--output"),
      List("CODE", "secret", "--output", "--repair")
    ).foreach { args =>
      intercept[IllegalArgumentException] {
        IconikChecker.parseOptions(args)
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
          repair = false,
          output,
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
      reportedIds(output.resolveSibling("report.missing.jsonl")) shouldBe empty
      reportedIds(
        output.resolveSibling("report.parent-mismatches.jsonl")
      ) shouldBe empty
      stdout.toString(UTF_8) shouldBe ""
      stderr.toString(UTF_8) shouldBe
        "\rICONIK [--------------------] 0/2 (0%)\rICONIK [##########----------] 1/2 (50%)\rICONIK [####################] 2/2 (100%)\nChecked 2 Iconik objects.\n"

      stderr.reset()
      Console.withErr(stderr) {
        IconikChecker.checkToFile(
          service,
          repair = false,
          output,
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
    }
  }

  it should "split defects into separate files, include overlapping defects and exclude healthy entities" in {
    val commissions = new IconikInMemoryStoreWithParentIndex[
      IconikCommission,
      DynamoReadError
    ]()
    List("missing", "mismatched", "healthy").foreach { id =>
      commissions.upsert(IconikCommission("group-1", id, id))
    }
    val service = new IconikValidationService(
      new IconikStore(
        new IconikInMemoryStoreWithParentIndex[
          IconikProject,
          DynamoReadError
        ](),
        commissions,
        new IconikInMemoryStore[IconikWorkingGroup, DynamoReadError]()
      ),
      new IconikCollectionClient {
        override def getCollection(id: String, credentials: IconikCredentials) =
          Future.successful(Right(id match {
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
        repair = false,
        output,
        () => (),
        ansi = false
      )
      reportedIds(output).toSet shouldBe Set("missing", "mismatched", "healthy")
      reportedIds(output.resolveSibling("report.missing.jsonl")) shouldBe List(
        "missing"
      )
      reportedIds(
        output.resolveSibling("report.name-mismatches.jsonl")
      ) shouldBe List("mismatched")
      reportedIds(
        output.resolveSibling("report.parent-mismatches.jsonl")
      ) shouldBe List("mismatched")
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
        repair = false,
        _ => fail("No report should be emitted"),
        () => fail("No pause should be needed"),
        ansi = false
      )
    }
    stderr.toString(UTF_8) shouldBe
      "\rICONIK [####################] 0/0 (100%)\nChecked 0 Iconik objects.\n"
  }

  it should "check entities sequentially without writing unless repair is enabled" in {
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
    def run(repair: Boolean): Unit =
      IconikChecker.check(
        service,
        repair,
        report => reports = reports :+ report,
        () => events = events :+ "pause"
      )

    run(repair = false)
    events shouldBe List("group-1", "pause", "group-2", "pause")
    reports.map(_.summary.nameMismatches) shouldBe List(1, 1)
    store
      .getWorkingGroup("group-1")
      .toOption
      .flatten
      .get
      .title shouldBe "Old title"

    run(repair = true)
    store
      .getWorkingGroup("group-1")
      .toOption
      .flatten
      .get
      .title shouldBe "New title"
    store
      .getWorkingGroup("group-2")
      .toOption
      .flatten
      .get
      .title shouldBe "New title"
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
        repair = false,
        _ => fail("No report should be emitted"),
        () => fail("No further checks should run")
      )
    }
    error.getMessage shouldBe "Iconik unavailable"
  }
}
