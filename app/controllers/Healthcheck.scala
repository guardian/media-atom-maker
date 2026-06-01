package controllers

import com.gu.media.aws.{AwsAccess, KinesisAccess}
import play.api.mvc.{BaseController, ControllerComponents}
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils

class Healthcheck(
    val kinesis: AwsAccess with KinesisAccess,
    val controllerComponents: ControllerComponents
) extends BaseController {
  def healthcheck = Action {
    val instanceId = EC2MetadataUtils.getInstanceId
    val isRunningInAws = instanceId != null
    val canAccessPreviewStream =
      kinesis.testKinesisAccess(kinesis.previewKinesisStreamName)
    val canAccessLiveStream =
      kinesis.testKinesisAccess(kinesis.liveKinesisStreamName)

    val isMissingTag = isRunningInAws && kinesis.isDev

    if (!isMissingTag && canAccessLiveStream && canAccessPreviewStream) {
      Ok(s"ok\ngitCommitID ${app.BuildInfo.gitCommitId}")
    } else {
      InternalServerError(
        "Fail. Cannot access CAPI kinesis streams or cannot read tags"
      )
    }
  }
}
