package com.gu.media.upload

import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import com.gu.media.aws.UploadAccess
import com.gu.media.logging.Logging
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

class CredentialsGenerator(aws: UploadAccess) extends Logging {
  def forPart(part: UploadPartKey): UploadCredentials = {
    val key = part.toString
    val keyPolicy = generateKeyPolicy(key)

    generateCredentials(part.id, key, keyPolicy)
  }

  private def generateCredentials(uploadId: String, key: String, keyPolicy: String): UploadCredentials = {
    val request = new AssumeRoleRequest()
      .withRoleArn(aws.userUploadRole)
      .withDurationSeconds(900) // 15 minutes (the minimum allowed in STS requests)
      .withPolicy(keyPolicy)
      .withRoleSessionName(s"media-atom-maker-upload-$uploadId")

    log.info(s"Issuing STS request for $key")
    val result = aws.uploadSTSClient.assumeRole(request)
    log.info(s"Received STS credentials for $key")

    val credentials = result.getCredentials

    UploadCredentials(credentials.getAccessKeyId, credentials.getSecretAccessKey, credentials.getSessionToken)
  }

  private def generateKeyPolicy(key: String): String = {
    val keyArn = s"arn:aws:s3:::${aws.userUploadBucket}/$key"

    val permissions = List("s3:PutObject", "s3:PutObjectAcl", "s3:ListMultipartUploadParts",
      "s3:AbortMultipartUpload", "s3:ListBucketMultipartUploads")

    val json = JsObject(List(
      "Statement" -> JsArray(List(
        JsObject(List(
          "Action" -> JsArray(permissions.map(JsString)),
          "Resource" -> JsString(keyArn),
          "Effect" -> JsString("Allow")
        ))
      ))
    ))

    Json.stringify(json)
  }
}
