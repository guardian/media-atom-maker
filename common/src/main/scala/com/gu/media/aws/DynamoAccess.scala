package com.gu.media.aws

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient

trait DynamoAccess { this: AwsAccess =>
  val dynamoTableName: String = getMandatoryString("aws.dynamo.tableName")
  val publishedDynamoTableName: String = getMandatoryString("aws.dynamo.publishedTableName")
  val auditDynamoTableName = getMandatoryString("aws.dynamo.auditTableName")

  lazy val dynamoDB: AmazonDynamoDBClient = region.createClient(
    classOf[AmazonDynamoDBClient],
    credsProvider,
    null
  )
}
