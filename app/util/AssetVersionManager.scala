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
    Map("UploadPipeline" -> UploadPipeline, "PreexistingAssetURL" -> PreexistingAssetURL)

  implicit val dynamoFormat: DynamoFormat[AssetClaimSource] =
    DynamoFormat.coercedXmap[AssetClaimSource, String, IllegalArgumentException](
      name => all.getOrElse(name, throw new IllegalArgumentException(s"Unknown AssetClaimSource: $name")),
      _.name
    )
}

class AssetVersionManager(
    awsConfig: AWSConfig,
    assetClaimSource: AssetClaimSource
) extends Logging {

  case class VersionClaim(id: String, claimSource: AssetClaimSource)

  object VersionClaim {
    def fromAtomIdAndVersionNumber(
        atomId: String,
        version: Long
    ): VersionClaim = {
      VersionClaim(s"$atomId-$version", assetClaimSource)
    }
  }

  @tailrec
  final def claimThisOrNextAvailableVersion(
      atomId: String,
      version: Long
  ): Long = {
    val assetsTable = Table[VersionClaim](awsConfig.assetsTableName)
    val claim =
      VersionClaim.fromAtomIdAndVersionNumber(atomId, version)
    val assetsResult = awsConfig.scanamo.exec(
      assetsTable.when(attributeNotExists("id")).put(claim)
    )

    assetsResult match {
      case Right(_) => version
      case Left(ConditionNotMet(_)) =>
        claimThisOrNextAvailableVersion(atomId, version + 1)
      case Left(_) =>
        log.warn(
          s"Unexpected error claiming version $version for atom $atomId. Retrying with next version."
        )
        claimThisOrNextAvailableVersion(atomId, version + 1)
    }
  }

}
