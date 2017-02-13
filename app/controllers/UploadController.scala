package controllers

import java.util.UUID
import javax.inject.Inject

import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import com.gu.pandahmac.HMACAuthActions
import model.{UploadCredentials, UploadPolicy}
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.api.mvc.Results.Ok
import util.{AWSConfig, Logging}

class UploadController @Inject ()(implicit val authActions: HMACAuthActions, val awsConfig: AWSConfig) extends Logging {
  import authActions.APIHMACAuthAction

  def create(atomId: String) = APIHMACAuthAction {
    log.info(s"Request for upload credentials for atom $atomId")

    val uploadId = UUID.randomUUID().toString
    val key = s"${awsConfig.userUploadFolder}/$atomId/$uploadId"

    log.info(s"Generated upload key $key for atom $atomId")

    val keyPolicy = generateKeyPolicy(key)

    log.info(s"Issuing STS request. uploadKey=$key. atomId=$atomId")
    val credentials = generateCredentials(uploadId, keyPolicy)
    log.info(s"Received STS credentials. uploadKey=$key. atomId=$atomId")

    val policy = UploadPolicy(awsConfig.userUploadBucket, key, awsConfig.region.toString, credentials)
    Ok(Json.toJson(policy))
  }

  private def generateCredentials(uploadId: String, keyPolicy: String): UploadCredentials = {
    val request = new AssumeRoleRequest()
      .withRoleArn(awsConfig.userUploadRole)
      .withDurationSeconds(900) // 15 minutes (the minimum allowed in STS requests)
      .withPolicy(keyPolicy)
      .withRoleSessionName(uploadId)

    val result = awsConfig.uploadSTSClient.assumeRole(request)
    val credentials = result.getCredentials

    UploadCredentials(credentials.getAccessKeyId, credentials.getSecretAccessKey, credentials.getSessionToken)
  }

  private def generateKeyPolicy(key: String): String = {
    val keyArn = s"arn:aws:s3:::${awsConfig.userUploadBucket}/$key"

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
