package com.gu.media.youtube

import com.google.api.services.youtubePartner.YouTubePartner
import com.google.api.services.youtubePartner.model._
import com.gu.media.logging.Logging
import com.typesafe.config.Config
import org.joda.time.DateTime
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

//This class contains functionality to add usage policies to published videos.
//Videos are either tracked or monetized: https://support.google.com/youtube/answer/107383?hl=en-GB
class YouTubeClaims(override val config: Config) extends YouTubeAccess with Logging {

  private def createAsset(title: String, videoId: String): Asset = {

    val metadata = new Metadata()
      .setTitle(title)
      .setDescription(videoId)

    val asset = new Asset()
      .setMetadata(metadata)
      .setType("web")

    val createdAsset = partnerClient.assets()
      .insert(asset)
      .setOnBehalfOfContentOwner(contentOwner)
      .execute()

    createdAsset

  }

  private def setOwnership(assetId: String) = {

    val territoryOwners = new TerritoryOwners()
      .setOwner(contentOwner)
      .setRatio(100.0)
      .setType("exclude")
      .setTerritories(List[String]())

    val ownership: RightsOwnership = new RightsOwnership().setGeneral(List(territoryOwners))

    partnerClient.ownership()
      .update(assetId, ownership)
      .setOnBehalfOfContentOwner(contentOwner)
      .execute()

  }

  private  def claimVideo(assetId: String, videoId: String, policy: Policy): Unit = {

    val claim = new Claim()
      .setAssetId((assetId))
      .setVideoId(videoId)
      .setPolicy(policy)
      .setContentType("audiovisual")

    val createdClaim = partnerClient.claims()
      .insert(claim)
      .setOnBehalfOfContentOwner(contentOwner)
      .execute()
  }

  private def getNewPolicy(blockAds: Boolean): Policy = {
    val policy = new Policy()
    if (blockAds)
      policy.setId(trackingPolicyId)
    else
      policy.setId(monetizationPolicyId)
  }

  private def createVideoClaim(atomId: String, userName: String, blockAds: Boolean, videoId: String): Unit = {
    val policy = getNewPolicy(blockAds)
    val assetTitle = s"$atomId-$userName-${DateTime.now()}"
    val assetId = createAsset(assetTitle, videoId).getId()
    setOwnership(assetId)
    claimVideo(assetId, videoId, policy)

  }

  private def updateClaim(claimId: String, assetId: String, videoId: String, blockAds: Boolean): Unit = {

    val policy = getNewPolicy(blockAds)

    val claim = new Claim()
      .setAssetId((assetId))
      .setVideoId(videoId)
      .setPolicy(policy)
      .setContentType("audiovisual")

    partnerClient.claims
      .patch(claimId, claim)
      .setOnBehalfOfContentOwner(contentOwner)
      .execute()
  }

  def createOrUpdateClaim(atomId: String, videoId: String, userName: String, blockAds: Boolean): Unit = {

    try {
      val videoClaims = partnerClient
        .claimSearch()
        .list
        .setVideoId(videoId)
        .execute()

      if (videoClaims.getPageInfo.getTotalResults() == 0) {
        createVideoClaim(atomId, userName, blockAds, videoId)
      } else {

        val claimsList = videoClaims.getItems().asScala

        claimsList.find(claimSnippet => !claimSnippet.getThirdPartyClaim()) match {
          case Some(claimSnippet) => {
            val claim = partnerClient.claims().get(claimSnippet.getId()).execute()
            val assetId = claim.getAssetId()
            val policy = claim.getPolicy().getId
            updateClaim(claim.getId(), assetId, videoId, blockAds)
          }
          case None => createVideoClaim(atomId, userName, blockAds, videoId)
        }
      }
    }
    catch {
      case e: Throwable => log.warn(s"unable to update claims for asset=$videoId ", e)
    }
  }
}
