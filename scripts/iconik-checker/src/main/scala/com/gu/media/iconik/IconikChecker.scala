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

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Using

object IconikChecker {
  private val usage =
    "usage: iconikChecker/runMain com.gu.media.iconik.IconikChecker CODE|PROD <secret-id> [--repair]"

  case class Options(stage: String, secretId: String, repair: Boolean)

  def parseOptions(args: List[String]): Options = args match {
    case stage :: secretId :: flags
        if Set("CODE", "PROD").contains(stage) &&
          secretId.nonEmpty &&
          (flags.isEmpty || flags == List("--repair")) =>
      Options(stage, secretId, flags.nonEmpty)
    case _ => throw new IllegalArgumentException(usage)
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
        check(
          service,
          options.repair,
          println,
          () => Thread.sleep(1000)
        )
      }.get
    } finally {
      Await.result(system.terminate(), 30.seconds)
    }
  }

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
      emit: String => Unit,
      pause: () => Unit
  ): Unit = {
    val entityIds = Await
      .result(service.listEntityIds(), 10.minutes)
      .fold(error => throw new IllegalStateException(error.message), identity)

    entityIds.foreach { entity =>
      val entityType = IconikEntityType
        .fromString(entity.entityType)
        .getOrElse(
          throw new IllegalArgumentException(
            s"Unknown Iconik entity type: ${entity.entityType}"
          )
        )
      Console.err.println(s"checking ${entity.entityType} ${entity.id}")
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
      emit(Json.stringify(report.toJson))
      pause()
    }
    Console.err.println(s"Checked ${entityIds.size} Iconik objects.")
  }
}
