package util

import com.gu.media.aws.{DynamoAccess, UploadAccess}
import com.gu.media.upload.model.Upload
import com.gu.scanamo.{Scanamo, Table}
import com.gu.scanamo.syntax._
import model.{ClientAsset, ClientAssetMetadata}

class UploadDecorator(aws: DynamoAccess with UploadAccess, stepFunctions: StepFunctions) {
  private val table = Table[Upload](aws.cacheTableName)

  def addMetadata(atomId: String, video: ClientAsset): ClientAsset = {
    val id = s"$atomId-${video.id}"

    getUpload(id) match {
      case Some((startTimestamp, upload)) =>
        video.copy(metadata = Some(
          ClientAssetMetadata(
            upload.metadata.originalFilename,
            startTimestamp,
            upload.metadata.user
          )
        ))

      case None =>
        video
    }
  }

  private def getUpload(id: String): Option[(Long, Upload)] = {
    // TODO MRB: startTimestamp
    stepFunctions.getById(id) orElse {
      val op = table.get('id -> id)
      val result = Scanamo.exec(aws.dynamoDB)(op)

      result.flatMap(_.right.toOption)
    }.map((0, _))
  }
}
