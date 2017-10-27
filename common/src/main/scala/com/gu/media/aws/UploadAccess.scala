package com.gu.media.aws

import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClient
import com.amazonaws.services.stepfunctions.model.ListStateMachinesRequest
import com.gu.media.Settings
import scala.collection.JavaConverters._

trait UploadAccess { this: Settings with AwsAccess =>
  val userUploadBucket: String = getMandatoryString("aws.upload.bucket")
  val userUploadFolder: String = getMandatoryString("aws.upload.folder")
  val userUploadRole: String = getMandatoryString("aws.upload.role")
  val syncWithPluto: Boolean = getBoolean("pluto.sync").getOrElse(false)

  val pipelineName: String = s"VideoPipeline$stage"
  lazy val pipelineArn: String = getPipelineArn()

  val cacheTableName: String = s"$app-cache-$stage"

  lazy val uploadSTSClient = createUploadSTSClient()

  lazy val stepFunctionsClient = region.createClient(
    classOf[AWSStepFunctionsClient],
    credsProvider,
    null
  )

  private def createUploadSTSClient() = {
    if(!userUploadRole.startsWith("arn:")) {
      throw new IllegalArgumentException("aws.upload.role must be in ARN format: arn:aws:iam::<account>:role/<role_name>")
    }

    region.createClient(classOf[AWSSecurityTokenServiceClient], credentials.upload, null)
  }

  private def getPipelineArn() = {
    // The name of the state machine in cloud formation changes on every deploy so we have to look it up
    val allMachines = stepFunctionsClient.listStateMachines(new ListStateMachinesRequest()).getStateMachines.asScala
    val ourMachine = allMachines.find(_.getName.contains(pipelineName)).map(_.getStateMachineArn)

    ourMachine.getOrElse {
      throw new IllegalStateException(s"Unable to find state machine $pipelineName in AWS")
    }
  }
}
