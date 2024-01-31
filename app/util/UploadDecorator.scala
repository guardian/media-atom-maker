package util

import com.gu.media.aws.{DynamoAccess, UploadAccess}
import com.gu.media.model.{ClientAsset, ClientAssetMetadata}
import com.gu.media.upload.model.Upload
import org.scanamo.Table
import org.scanamo.syntax._
import org.scanamo.generic.auto._

class UploadDecorator(aws: DynamoAccess with UploadAccess, stepFunctions: StepFunctions) {
  private val table = Table[Upload](aws.cacheTableName)

  def addMetadata(atomId: String, video: ClientAsset): ClientAsset = {
    val id = s"$atomId-${video.id}"

    getUpload(id) match {
      case Some(upload) =>
        video.copy(metadata = Some(
          ClientAssetMetadata(
            upload.metadata.originalFilename,
            upload.metadata.startTimestamp,
            upload.metadata.user
          )
        ))

      case None =>
        video
    }
  }

  private def getUpload(id: String): Option[Upload] =
    aws.scanamo.exec(table.get("id" === id)).flatMap(_.toOption) orElse { stepFunctions.getById(id) }
}
