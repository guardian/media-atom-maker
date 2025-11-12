package com.gu.media.aws

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.gu.media.Settings
import com.gu.media.aws.AwsV2Util.buildSync
import org.scanamo.Scanamo
import software.amazon.awssdk.services.dynamodb.{
  DynamoDbClient,
  DynamoDbClientBuilder
}

trait DynamoAccess { this: Settings with AwsAccess =>
  lazy val dynamoTableName: String = sys.env.getOrElse(
    "ATOM_TABLE_NAME",
    getMandatoryString("aws.dynamo.tableName")
  )

  lazy val publishedDynamoTableName: String = getMandatoryString(
    "aws.dynamo.publishedTableName"
  )

  lazy val manualPlutoDynamo: String = sys.env.getOrElse(
    "PLUTO_TABLE_NAME",
    getMandatoryString("aws.dynamo.plutoTableName")
  )

  private def getTableName(name: String): String = s"$app-$stage-$name-table"

  lazy val plutoCommissionTableName: String = getTableName("pluto-commissions")
  lazy val plutoProjectTableName: String = getTableName("pluto-projects")
  lazy val iconikWorkingGroupTableName: String =
    if (stage == "DEV") "media-atom-maker-CODE-iconik-working-groups-table"
    else
      getTableName("iconik-working-groups")
  lazy val iconikCommissionTableName: String =
    if (stage == "DEV") "media-atom-maker-CODE-iconik-commissions-table"
    else
      getTableName(
        "iconik-commissions"
      )
  lazy val iconikProjectTableName: String =
    if (stage == "DEV") "media-atom-maker-CODE-iconik-projects-table"
    else getTableName("iconik-projects")

  lazy val dynamoDB = AmazonDynamoDBClientBuilder
    .standard()
    .withCredentials(credsProvider)
    .withRegion(region.getName)
    .build()

  lazy val dynamoDbSdkV2: DynamoDbClient =
    buildSync[DynamoDbClient, DynamoDbClientBuilder](
      DynamoDbClient.builder(),
      credentials.instance.awsV2Creds,
      awsV2Region
    )

  lazy val scanamo: Scanamo = Scanamo(dynamoDbSdkV2)
}
