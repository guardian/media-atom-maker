package com.gu.media.youtube

import com.google.api.services.youtubePartner.model._
import com.typesafe.config.Config
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

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

  private def getPolicy(addsTurnedOff: Boolean): Policy = {
    val policy = new Policy()
    if (addsTurnedOff)
      policy.setId(trackingPolicyId)
    else
      policy.setId(monetizationPolicyId)
  }

  def setVideoClaim(assetTitle: String, assetDescription: String, addsTurnedOff: Boolean, videoId: String): Unit = {

    val policy = getPolicy(addsTurnedOff)
    val assetId = createAsset(assetTitle, assetDescription)
    setOwnership(assetId)

    claimVideo(assetId, videoId, policy)

  }

  def getExistingClaimAndPolicyIds(videoId: String): Option[(String, String)] = {

    val claims = partnerClient.claims.list().execute().getItems().asScala

    claims.find(_.getId() == videoId).flatMap(claim => {
      Some((claim.getId(), claim.getAssetId()))
    })
  }

  def updateClaim(claimId: String, assetId: String, videoId: String, addsTurnedOff: Boolean): Unit = {

    val policy = getPolicy(addsTurnedOff)

    val claim = new Claim()
      .setAssetId((assetId))
      .setVideoId(videoId)
      .setPolicy(policy)
      .setContentType("audivisual")

    partnerClient.claims
      .update(claimId, claim)
      .setOnBehalfOfContentOwner(contentOwner)

  }

}
