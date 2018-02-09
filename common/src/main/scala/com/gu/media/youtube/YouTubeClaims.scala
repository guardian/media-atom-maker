package com.gu.media.youtube

import com.google.api.client.googleapis.json.{GoogleJsonError, GoogleJsonResponseException}
import com.google.api.services.youtubePartner.YouTubePartner
import com.google.api.services.youtubePartner.model._
import com.gu.media.logging.Logging
import com.gu.media.model.VideoUpdateError

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

//This class contains functionality to add usage policies to published videos.
//Videos are either tracked or monetized: https://support.google.com/youtube/answer/107383?hl=en-GB
trait YouTubeClaims { this: YouTubeAccess with Logging =>

  private def createAsset(title: String, videoId: String, atomId: String): Either[VideoUpdateError, Asset] = {

    val metadata = new Metadata()
      .setTitle(title)
      .setDescription(videoId)
      .setCustomId(atomId)

    val asset = new Asset()
      .setMetadata(metadata)
      .setType("web")

    try {
      val createdAsset = partnerClient.assets()
        .insert(asset)
        .setOnBehalfOfContentOwner(contentOwner)
        .execute()

      Right(createdAsset)
    } catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        Left(VideoUpdateError(s"Error in creating an asset: ${error.toString()}", Some(error.getMessage)))
      }
    }
  }

  private def setOwnership(assetId: String): Either[VideoUpdateError, RightsOwnership] = {

    val territoryOwners = new TerritoryOwners()
      .setOwner(contentOwner)
      .setRatio(100.0)
      .setType("exclude")
      .setTerritories(List[String]())

    val createdOwnership: RightsOwnership = new RightsOwnership().setGeneral(List(territoryOwners))

    try {
      val ownership = partnerClient.ownership()
        .update(assetId, createdOwnership)
        .setOnBehalfOfContentOwner(contentOwner)
        .execute()

      Right(ownership)
    } catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        Left(VideoUpdateError(s"Error in setting claim ownership: ${error.toString()}", Some(error.getMessage)))
      }
    }

  }

  private def claimVideo(assetId: String, videoId: String, policy: Policy): Either[VideoUpdateError, String] = {

    val claim = new Claim()
      .setAssetId(assetId)
      .setVideoId(videoId)
      .setPolicy(policy)
      .setContentType("audiovisual")

    try {

      val newClaim: Claim = partnerClient.claims()
        .insert(claim)
        .setOnBehalfOfContentOwner(contentOwner)
        .execute()

      Right(s"No partner claim found, claimed video ${videoId} with a new claim ${newClaim.getId}")

    } catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        Left(VideoUpdateError(s"Error in claiming video with new claim: ${error.toString()}", Some(error.getMessage)))
      }
    }

  }

  private def getNewPolicy(blockAds: Boolean): Policy = {
    val policy = new Policy()
    if (blockAds)
      policy.setId(trackingPolicyId)
    else
      policy.setId(monetizationPolicyId)
  }

  private def createVideoClaim(atomId: String, blockAds: Boolean, videoId: String): Either[VideoUpdateError, String] = {
    val policy = getNewPolicy(blockAds)
    val assetTitle = s"media-atom-maker_atom=${atomId}_video=${videoId}"
    createAsset(assetTitle, videoId, atomId) match {
      case Right(asset) => {
        val assetId = asset.getId
        setOwnership(assetId) match {
          case Right(ownership) => claimVideo(assetId, videoId, policy)
          case Left(error) => Left(error)
        }
      } case Left(error) => Left(error)
    }
  }

  private def updateClaim(claimId: String, assetId: String, videoId: String, blockAds: Boolean): Either[VideoUpdateError, String] = {
    val policy = getNewPolicy(blockAds)

    val claim = new Claim()
      .setAssetId(assetId)
      .setVideoId(videoId)
      .setPolicy(policy)
      .setContentType("audiovisual")

    try {

      partnerClient.claims
        .patch(claimId, claim)
        .setOnBehalfOfContentOwner(contentOwner)
        .execute()

      Right(s"Updated claim for claim=$claimId asset=$assetId")

    } catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        Left(VideoUpdateError(s"Error in claiming a video: ${error.toString()}", Some(error.getMessage)))
      }
    }
  }

  private def getPagedVideoClaims(request: YouTubePartner#ClaimSearch#List, items: List[ClaimSnippet]):
    List[ClaimSnippet] = {

    val response = request.execute()

    val allItems = if (response.getPageInfo.getTotalResults == 0) items
      else items ++ response.getItems.asScala.toList

    response.getNextPageToken match {
      case null => allItems
      case token => {
        val nextRequest = request.setPageToken(token)
        getPagedVideoClaims(nextRequest, allItems)
      }
    }
  }

  def createOrUpdateClaim(atomId: String, videoId: String, blockAds: Boolean): Either[VideoUpdateError, String] = {

    val request: YouTubePartner#ClaimSearch#List = partnerClient
      .claimSearch()
      .list
      .setVideoId(videoId)

    try {

      val videoClaims = getPagedVideoClaims(request, List())

      videoClaims.find(claimSnippet => !claimSnippet.getThirdPartyClaim) match {
        case Some(claimSnippet) => {
          val claim = partnerClient.claims().get(claimSnippet.getId).execute()
          val claimId = claim.getId
          val assetId = claim.getAssetId
          updateClaim(claimId, assetId, videoId, blockAds)
        }
        case None => createVideoClaim(atomId, blockAds, videoId)
      }
    }

    catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        Left(VideoUpdateError(error.toString, Some(error.getMessage)))
      }
    }
  }

  def getAdvertisingOptions(videoId: String): VideoAdvertisingOption = {
    partnerClient.videoAdvertisingOptions().get(videoId).execute()
  }
}
