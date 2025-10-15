package com.gu.media.aws

import com.amazonaws.services.securitytoken.{
  AWSSecurityTokenService,
  AWSSecurityTokenServiceClientBuilder
}
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder
import com.amazonaws.services.stepfunctions.model.ListStateMachinesRequest
import com.gu.media.Settings

import scala.jdk.CollectionConverters._

trait UploadAccess { this: Settings with AwsAccess =>
  val userUploadBucket: String = getMandatoryString("aws.upload.bucket")
  val userUploadFolder: String = getMandatoryString("aws.upload.folder")
  val userUploadRole: String = getMandatoryString("aws.upload.role")

  val pipelineName: String = s"VideoPipeline$stage"
  lazy val pipelineArn: String = getPipelineArn()

  val cacheTableName: String = s"media-atom-pipeline-cache-$stage"

  lazy val uploadSTSClient = createUploadSTSClient()

  lazy val stepFunctionsClient = AWSStepFunctionsClientBuilder
    .standard()
    .withCredentials(credsProvider)
    .withRegion(region.getName)
    .build()

  private def createUploadSTSClient(): AWSSecurityTokenService = {
    if (!userUploadRole.startsWith("arn:")) {
      throw new IllegalArgumentException(
        "aws.upload.role must be in ARN format: arn:aws:iam::<account>:role/<role_name>"
      )
    }

    AWSSecurityTokenServiceClientBuilder
      .standard()
      .withCredentials(credentials.upload.awsV1Creds)
      .withRegion(region.getName)
      .build()
  }

  private def getPipelineArn() = {
    // The name of the state machine in cloud formation changes on every deploy so we have to look it up
    val allMachines = stepFunctionsClient
      .listStateMachines(new ListStateMachinesRequest())
      .getStateMachines
      .asScala
    val ourMachine = allMachines
      .find(_.getName.contains(pipelineName))
      .map(_.getStateMachineArn)

    ourMachine.getOrElse {
      throw new IllegalStateException(
        s"Unable to find state machine $pipelineName in AWS"
      )
    }
  }
}
