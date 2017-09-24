package controllers

import com.gu.media.aws.KinesisAccess
import play.api.mvc.{Action, Controller}

class Healthcheck(val kinesis: KinesisAccess) extends Controller {
  def healthcheck = Action {
    if(kinesis.testKinesisAccess(kinesis.previewKinesisStreamName) && kinesis.testKinesisAccess(kinesis.liveKinesisStreamName)) {
      Ok(s"ok\ngitCommitID ${app.BuildInfo.gitCommitId}")
    } else {
      InternalServerError("fail. cannot access CAPI kinesis streams")
    }
  }
}
