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

  private def getTableName(itemType: String, stage: String): String =
    s"$app-$stage-$itemType-table"

  lazy val plutoCommissionTableName: String =
    getTableName("pluto-commissions", stage = stage)
  lazy val plutoProjectTableName: String =
    getTableName("pluto-projects", stage = stage)
  lazy val iconikWorkingGroupTableName: String =
    getTableName(
      "iconik-working-groups",
      stage = if (stage == "DEV") "CODE" else stage
    )
  lazy val iconikCommissionTableName: String =
    getTableName(
      "iconik-commissions",
      stage = if (stage == "DEV") "CODE" else stage
    )
  lazy val iconikProjectTableName: String =
    getTableName(
      "iconik-projects",
      stage = if (stage == "DEV") "CODE" else stage
    )

  lazy val dynamoDbSdkV2: DynamoDbClient =
    buildSync[DynamoDbClient, DynamoDbClientBuilder](
      DynamoDbClient.builder(),
      credentials.instance.awsV2Creds,
      awsV2Region
    )

  lazy val scanamo: Scanamo = Scanamo(dynamoDbSdkV2)
}
