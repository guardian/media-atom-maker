package com.gu.media.youtube

import com.google.api.services.youtubePartner.model._
import com.typesafe.config.Config
import scala.collection.JavaConversions._

class YouTubeClaims(override val config: Config) extends YouTubeAccess {

  private def createAsset(title: String, description: String): String = {

    val metadata = new Metadata()
      .setTitle(title)
      .setDescription(description)

    val asset = new Asset()
      .setMetadata(metadata)
      .setType("web")

    val createdAsset = partnerClient.assets()
      .insert(asset)
      .setOnBehalfOfContentOwner(contentOwner)
      .execute()

    createdAsset.getId()

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

  private  def claimVideo(assetId: String, videoId: String, policy: Policy) = {

    val claim = new Claim()
      .setAssetId((assetId))
      .setVideoId(videoId)
      .setPolicy(policy)
      .setContentType("audivisual")

    partnerClient.claims()
      .insert(claim)
      .setOnBehalfOfContentOwner(contentOwner)
  }

  def setVideoClaim(assetTitle: String, assetDescription: String, addsTurnedOff: Boolean, videoId: String): Unit = {

    val policy = new Policy()
    if (addsTurnedOff)
      policy.setId(trackingPolicyId)
    else
      policy.setId(monetizationPolicyId)

    val assetId = createAsset(assetTitle, assetDescription)
    setOwnership(assetId)

    claimVideo(assetId, videoId, policy)

  }

}
