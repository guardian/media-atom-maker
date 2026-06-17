package util

import org.scanamo.{ConditionNotMet, Table}
import org.scanamo.syntax.attributeNotExists
import org.scanamo.generic.auto._
import com.gu.media.logging.Logging

import scala.annotation.tailrec

class AssetVersionManager(awsConfig: AWSConfig) extends Logging {

  case class VersionClaim(assetId: String)

  object VersionClaim {
    def fromAtomIdAndVersionNumber(
        atomId: String,
        version: Long
    ): VersionClaim = {
      VersionClaim(s"$atomId-$version")
    }
  }

  @tailrec
  final def claimNextVersion(atomId: String, version: Long): Long = {
    val assetsTable = Table[VersionClaim](awsConfig.assetsTableName)
    val claim = VersionClaim.fromAtomIdAndVersionNumber(atomId, version)
    val assetsResult = awsConfig.scanamo.exec(
      assetsTable.when(attributeNotExists("id")).put(claim)
    )

    assetsResult match {
      case Right(_) => version
      case Left(ConditionNotMet(_)) =>
        claimNextVersion(atomId, version + 1)
      case Left(_) =>
        log.warn(
          s"Unexpected error claiming version $version for atom $atomId. Retrying with next version."
        )
        claimNextVersion(atomId, version + 1)
    }
  }

}
