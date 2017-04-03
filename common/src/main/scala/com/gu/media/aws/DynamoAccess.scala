package com.gu.media.aws

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.media.Settings

trait DynamoAccess { this: Settings with AwsAccess =>
  lazy val dynamoTableName: String = getMandatoryString("aws.dynamo.tableName")
  lazy val publishedDynamoTableName: String = getMandatoryString("aws.dynamo.publishedTableName")
  lazy val auditDynamoTableName: String = getMandatoryString("aws.dynamo.auditTableName")
  lazy val uploadTrackingTableName: String = sys.env.getOrElse("UPLOAD_TRACKING_TABLE_NAME",
    getMandatoryString("aws.dynamo.uploadTrackingTableName")
  )
  lazy val manualPlutoDynamo = getMandatoryString("aws.dynamo.plutoTableName")

  private def getTableName(name: String): String = s"$app-$stage-$name-table"

  lazy val plutoProjectTableName: String = getTableName("pluto-projects")

  lazy val dynamoDB: AmazonDynamoDBClient = region.createClient(
    classOf[AmazonDynamoDBClient],
    credsProvider,
    null
  )
}
