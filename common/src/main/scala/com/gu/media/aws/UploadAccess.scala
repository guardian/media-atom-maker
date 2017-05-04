package com.gu.media.aws

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient
import com.gu.media.Settings

trait UploadAccess { this: Settings with AwsAccess =>
  val userUploadBucket: String = getMandatoryString("aws.upload.bucket")
  val userUploadFolder: String = getMandatoryString("aws.upload.folder")
  val userUploadRole: String = getMandatoryString("aws.upload.role")

  lazy val uploadSTSClient = createUploadSTSClient()

  private def createUploadSTSClient() = {
    if(!userUploadRole.startsWith("arn:")) {
      throw new IllegalArgumentException("aws.upload.role must be in ARN format: arn:aws:iam::<account>:role/<role_name>")
    }

    region.createClient(classOf[AWSSecurityTokenServiceClient], credentials.upload, null)
  }
}
