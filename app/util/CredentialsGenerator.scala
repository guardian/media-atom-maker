package util

import com.gu.media.aws.UploadAccess
import com.gu.media.logging.Logging
import com.gu.media.upload.model.UploadCredentials
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

class CredentialsGenerator(aws: UploadAccess) extends Logging {
  def forKey(key: String): UploadCredentials = {
    val keyPolicy = generateKeyPolicy(key)

    generateCredentials(key, keyPolicy)
  }

  private def generateCredentials(
      key: String,
      keyPolicy: String
  ): UploadCredentials = {
    val request = AssumeRoleRequest
      .builder()
      .roleArn(aws.userUploadRole)
      .durationSeconds(
        900
      ) // 15 minutes (the minimum allowed in STS requests)
      .policy(keyPolicy)
      .roleSessionName(s"media-atom-pipeline")
      .build()

    log.info(s"Issuing STS request for $key")
    val result = aws.uploadSTSClient.assumeRole(request)
    log.info(s"Received STS credentials for $key")

    val credentials = result.credentials()

    UploadCredentials(
      credentials.accessKeyId(),
      credentials.secretAccessKey(),
      credentials.sessionToken()
    )
  }

  private def generateKeyPolicy(key: String): String = {
    val keyArn = s"arn:aws:s3:::${aws.userUploadBucket}/$key"

    val permissions = List(
      "s3:PutObject",
      "s3:PutObjectAcl",
      "s3:ListMultipartUploadParts",
      "s3:AbortMultipartUpload",
      "s3:ListBucketMultipartUploads"
    )

    val json = JsObject(
      List(
        "Statement" -> JsArray(
          List(
            JsObject(
              List(
                "Action" -> JsArray(permissions.map(JsString)),
                "Resource" -> JsString(keyArn),
                "Effect" -> JsString("Allow")
              )
            )
          )
        )
      )
    )

    Json.stringify(json)
  }
}
