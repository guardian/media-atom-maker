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

import scala.concurrent.{ExecutionContext, Future}

class IconikCheckerTest extends AnyFlatSpec with Matchers {
  private implicit val executionContext: ExecutionContext =
    ExecutionContext.global

  "IconikChecker" should "require an explicit stage and opt in to repairs" in {
    IconikChecker.parseOptions(List("CODE", "secret")).repair shouldBe false
    IconikChecker
      .parseOptions(List("PROD", "secret", "--repair"))
      .repair shouldBe true
    List(
      Nil,
      List("DEV", "secret"),
      List("CODE", "secret", "--unknown"),
      List("CODE", "secret", "--repair", "extra")
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
      IconikChecker.Options("CODE", "secret", repair = false),
      provider,
      Region.EU_WEST_1
    )
    aws.stage shouldBe "CODE"
    aws.iconikProjectTableName shouldBe "media-atom-maker-CODE-iconik-projects-table-v2"
    aws.iconikCommissionTableName shouldBe "media-atom-maker-CODE-iconik-commissions-table-v2"
    aws.iconikWorkingGroupTableName shouldBe "media-atom-maker-CODE-iconik-working-groups-table-v2"
    aws.credentials.instance.awsV2Creds shouldBe provider
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
    var reports = List.empty[String]
    def run(repair: Boolean): Unit =
      IconikChecker.check(
        service,
        repair,
        report => reports = reports :+ report,
        () => events = events :+ "pause"
      )

    run(repair = false)
    events shouldBe List("group-1", "pause", "group-2", "pause")
    reports.map(report =>
      (Json.parse(report) \ "summary" \ "nameMismatches").as[Int]
    ) shouldBe List(1, 1)
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
