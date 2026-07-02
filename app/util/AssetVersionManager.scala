package util

import org.scanamo.{ConditionNotMet, DynamoFormat, Table}
import org.scanamo.syntax.attributeNotExists
import org.scanamo.generic.auto._
import com.gu.media.logging.Logging

import scala.annotation.tailrec

sealed trait AssetClaimSource {
  def name: String
}
object AssetClaimSource {
  case object UploadPipeline extends AssetClaimSource {
    override def name: String = "UploadPipeline"
  }
  case object PreexistingAssetURL extends AssetClaimSource {
    override def name: String = "PreexistingAssetURL"
  }

  private val all: Map[String, AssetClaimSource] =
    Map(
      "UploadPipeline" -> UploadPipeline,
      "PreexistingAssetURL" -> PreexistingAssetURL
    )

  implicit val dynamoFormat: DynamoFormat[AssetClaimSource] =
    DynamoFormat
      .coercedXmap[AssetClaimSource, String, IllegalArgumentException](
        name =>
          all.getOrElse(
            name,
            throw new IllegalArgumentException(
              s"Unknown AssetClaimSource: $name"
            )
          ),
        _.name
      )
}

class AssetVersionManager(
    awsConfig: AWSConfig,
    assetClaimSource: AssetClaimSource
) extends Logging {

  case class VersionClaim(
      id: String,
      claimSource: AssetClaimSource,
      claimedAtTimestamp: Long, // Unix timestamp in milliseconds
      claimedByUser: String,
      originalFilename: Option[String]
  )

  @tailrec
  final def claimThisOrNextAvailableVersion(
      atomId: String,
      version: Long,
      userEmail: String,
      originalFilename: Option[String]
  ): Long = {
    val assetsTable = Table[VersionClaim](awsConfig.assetsTableName)
    val claim = VersionClaim(
      id = s"$atomId-$version",
      claimSource = assetClaimSource,
      claimedAtTimestamp = System.currentTimeMillis(),
      claimedByUser = userEmail,
      originalFilename = originalFilename
    )
    val assetsResult = awsConfig.scanamo.exec(
      assetsTable.when(attributeNotExists("id")).put(claim)
    )

    assetsResult match {
      case Right(_) => version
      case Left(ConditionNotMet(_)) =>
        claimThisOrNextAvailableVersion(
          atomId = atomId,
          version = version + 1,
          userEmail = userEmail,
          originalFilename = originalFilename
        )
      case Left(_) =>
        log.warn(
          s"Unexpected error claiming version $version for atom $atomId. Retrying with next version."
        )
        claimThisOrNextAvailableVersion(
          atomId = atomId,
          version = version + 1,
          userEmail = userEmail,
          originalFilename = originalFilename
        )
    }
  }

}
