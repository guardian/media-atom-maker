package com.gu.media.aws

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.media.Settings

trait DynamoAccess { this: Settings with AwsAccess =>
  lazy val dynamoTableName: String = sys.env.getOrElse("ATOM_TABLE_NAME",
    getMandatoryString("aws.dynamo.tableName")
  )

  lazy val publishedDynamoTableName: String = getMandatoryString("aws.dynamo.publishedTableName")

  lazy val auditDynamoTableName: String = sys.env.getOrElse("AUDIT_TABLE_NAME",
    getMandatoryString("aws.dynamo.auditTableName")
  )

  lazy val manualPlutoDynamo: String = sys.env.getOrElse("PLUTO_TABLE_NAME",
    getMandatoryString("aws.dynamo.plutoTableName")
  )

  private def getTableName(name: String): String = s"$app-$stage-$name-table"

  lazy val plutoCommissionTableName: String = getTableName("pluto-commissions")
  lazy val plutoProjectTableName: String = getTableName("pluto-projects")

  lazy val dynamoDB: AmazonDynamoDBClient = region.createClient(
    classOf[AmazonDynamoDBClient],
    credsProvider,
    null
  )
}
