package com.gu.media.youtube

import com.google.api.services.youtubePartner.model._
import com.gu.media.logging.Logging
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

trait Monetization { this: YouTubeAccess with Logging =>

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

  private def getPolicyId(name: String): Option[Policy] = {
    val policies = partnerClient.policies
      .list()
      .execute()
      .getItems()
      .asScala

    policies.find(policy => {
      policy.getName() == name })

  }

  def setVideoUsage(assetTitle: String, assetDescription: String, policyName: String, videoId: String): Unit = {

    val assetId = createAsset(assetTitle, assetDescription)
    setOwnership(assetId)
    getPolicyId(policyName) match {
      case Some(policy) => claimVideo(assetId, videoId, policy)
      case None => {
        log.warn(s"Could not add asset: could not find policy with name ${policyName}")
        throw new Exception(s"Failed to find policy with name ${policyName}")
      }
    }
  }

}
