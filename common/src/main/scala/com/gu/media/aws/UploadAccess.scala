package com.gu.media.aws

import com.gu.media.Settings

trait UploadAccess { this: Settings with AwsAccess =>
  val userUploadBucket: String = getMandatoryString("aws.upload.bucket")
  val userUploadFolder: String = getMandatoryString("aws.upload.folder")
  val userUploadRole: String = getMandatoryString("aws.upload.role")
  val selfHostedOrigin: String = getMandatoryString("aws.upload.selfHostedOrigin")
}
