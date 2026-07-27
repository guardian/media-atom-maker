package util

import org.scanamo.{ConditionNotMet, DynamoFormat, ScanamoError, Table}
import org.scanamo.syntax.attributeNotExists
import org.scanamo.generic.auto._
import com.gu.media.logging.Logging

import scala.annotation.tailrec

case class AssetVersionClaimError(
    atomId: String,
    version: Long
) {
  def message: String =
    s"atom $atomId, version $version could not be claimed due to a DynamoDB error"
}

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
  ): Either[AssetVersionClaimError, Long] = {
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
      case Right(_) => Right(version)
      case Left(ConditionNotMet(_)) =>
        claimThisOrNextAvailableVersion(
          atomId = atomId,
          version = version + 1,
          userEmail = userEmail,
          originalFilename = originalFilename
        )
      case Left(error) =>
        log.error(
          s"Unexpected error claiming version $version for atom $atomId: $error"
        )
        Left(AssetVersionClaimError(atomId, version))
    }
  }

}
