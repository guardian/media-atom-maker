package com.gu.media.aws

import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sfn.model.ListStateMachinesRequest
import com.gu.media.Settings
import software.amazon.awssdk.services.sts.StsClient

import scala.jdk.CollectionConverters._

trait UploadAccess { this: Settings with AwsAccess =>
  val userUploadBucket: String = getMandatoryString("aws.upload.bucket")
  val userUploadFolder: String = getMandatoryString("aws.upload.folder")
  val userUploadRole: String = getMandatoryString("aws.upload.role")

  val pipelineName: String = s"VideoPipeline$stage"
  lazy val pipelineArn: String = getPipelineArn()

  val cacheTableName: String = s"media-atom-pipeline-cache-$stage"

  lazy val uploadSTSClient = createUploadSTSClient()

  lazy val stepFunctionsClient = SfnClient
    .builder()
    .credentialsProvider(credentials.instance.awsV2Creds)
    .region(awsV2Region)
    .build()

  private def createUploadSTSClient(): StsClient = {
    if (!userUploadRole.startsWith("arn:")) {
      throw new IllegalArgumentException(
        "aws.upload.role must be in ARN format: arn:aws:iam::<account>:role/<role_name>"
      )
    }

    StsClient
      .builder()
      .credentialsProvider(credentials.upload.awsV2Creds)
      .region(awsV2Region)
      .build()
  }

  private def getPipelineArn() = {
    // The name of the state machine in cloud formation changes on every deploy so we have to look it up
    val allMachines = stepFunctionsClient
      .listStateMachines(ListStateMachinesRequest.builder().build())
      .stateMachines
      .asScala
      .toList
    val ourMachine = allMachines
      .find(_.name.contains(pipelineName))
      .map(_.stateMachineArn)

    ourMachine.getOrElse {
      throw new IllegalStateException(
        s"Unable to find state machine $pipelineName in AWS"
      )
    }
  }
}
