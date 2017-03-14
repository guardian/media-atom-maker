package com.gu.media.aws

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.media.Settings

trait DynamoAccess { this: Settings with AwsAccess =>
  val dynamoTableName: String = getMandatoryString("aws.dynamo.tableName")
  val publishedDynamoTableName: String = getMandatoryString("aws.dynamo.publishedTableName")
  val auditDynamoTableName: String = getMandatoryString("aws.dynamo.auditTableName")
  val uploadTrackingTableName: String = getMandatoryString("aws.dynamo.uploadTrackingTableName")

  lazy val dynamoDB: AmazonDynamoDBClient = region.createClient(
    classOf[AmazonDynamoDBClient],
    credsProvider,
    null
  )
}
