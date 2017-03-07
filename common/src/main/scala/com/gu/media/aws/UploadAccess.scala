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
    val provider = stage match {
      case "DEV" =>
        // Only required in dev. Instance profile credentials are sufficient when deployed
        val accessKey = getMandatoryString("aws.upload.accessKey", "This is the AwsId output of the dev cloudformation")
        val secretKey = getMandatoryString("aws.upload.secretKey", "This is the AwsSecret output of the dev cloudformation")

        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))

      case _ =>
        credsProvider
    }

    region.createClient(classOf[AWSSecurityTokenServiceClient], provider, null)
  }
}
