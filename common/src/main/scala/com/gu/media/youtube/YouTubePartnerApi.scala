package com.gu.media.youtube

import java.util
import com.google.api.client.googleapis.json.{GoogleJsonError, GoogleJsonResponseException}
import com.google.api.services.youtubePartner.YouTubePartner
import com.google.api.services.youtubePartner.model._
import com.gu.media.logging.{Logging, YoutubeApiType, YoutubeRequestLogger, YoutubeRequestType}
import com.gu.media.model.{AdSettings, VideoUpdateError}
import com.gu.media.util.MAMLogger

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

//This class contains functionality to add usage policies to published videos.
//Videos are either tracked or monetized: https://support.google.com/youtube/answer/107383?hl=en-GB
trait YouTubePartnerApi { this: YouTubeAccess with Logging =>

  private def createAsset(title: String, videoId: String, atomId: String): Either[VideoUpdateError, Asset] = {
    MAMLogger.info(s"Creating YouTube asset for $videoId", atomId, videoId)
    val metadata = new Metadata()
      .setTitle(title)
      .setDescription(videoId)
      .setCustomId(atomId)

    val asset = new Asset()
      .setMetadata(metadata)
      .setType("web")

    try {
      val request = partnerClient.assets()
        .insert(asset)
        .setOnBehalfOfContentOwner(contentOwner)

      YoutubeRequestLogger.logRequest(YoutubeApiType.PartnerApi, YoutubeRequestType.CreateAsset)
      val createdAsset = request.execute()
      MAMLogger.info(s"YouTube asset created successfully for $videoId", atomId, videoId)
      Right(createdAsset)
    } catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        MAMLogger.error(s"Failed to create asset for $videoId. ${error.toString} ${error.getMessage}", atomId, videoId)
        Left(VideoUpdateError(s"Error in creating an asset: ${error.toString()}", Some(error.getMessage)))
      }
    }
  }

  private def setOwnership(atomId: String, videoId: String, assetId: String): Either[VideoUpdateError, RightsOwnership] = {
    MAMLogger.info(s"Setting ownership for YouTube asset $assetId", atomId, videoId)
    val territoryOwners = new TerritoryOwners()
      .setOwner(contentOwner)
      .setRatio(100.0)
      .setType("exclude")
      .setTerritories(List[String]())

    val createdOwnership: RightsOwnership = new RightsOwnership().setGeneral(List(territoryOwners))

    try {
      val request = partnerClient.ownership()
        .update(assetId, createdOwnership)
        .setOnBehalfOfContentOwner(contentOwner)

      YoutubeRequestLogger.logRequest(YoutubeApiType.PartnerApi, YoutubeRequestType.SetOwnership)
      val ownership = request.execute()
      MAMLogger.info(s"Successfully set ownership of asset $assetId", atomId, videoId)
      Right(ownership)
    } catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        MAMLogger.error(s"Failed to set ownership of asset $assetId. ${error.toString} ${error.getMessage}", atomId, videoId)
        Left(VideoUpdateError(s"Error in setting claim ownership: ${error.toString()}", Some(error.getMessage)))
      }
    }

  }

  private def claimVideo(atomId: String, assetId: String, videoId: String, policy: Policy): Either[VideoUpdateError, String] = {
    MAMLogger.info(s"Claiming video $videoId", atomId, videoId)
    val claim = new Claim()
      .setAssetId(assetId)
      .setVideoId(videoId)
      .setPolicy(policy)
      .setContentType("audiovisual")

    try {
      val request = partnerClient.claims()
        .insert(claim)
        .setOnBehalfOfContentOwner(contentOwner)

      YoutubeRequestLogger.logRequest(YoutubeApiType.PartnerApi, YoutubeRequestType.CreateVideoClaim)
      val newClaim: Claim = request.execute()
      MAMLogger.info(s"Successfully claimed video $videoId", atomId, videoId)
      Right(s"No partner claim found, claimed video ${videoId} with a new claim ${newClaim.getId}")

    } catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        MAMLogger.error(s"Failed to claim video $videoId. ${error.toString} ${error.getMessage}", atomId, videoId)
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
        setOwnership(atomId, videoId, assetId) match {
          case Right(ownership) => claimVideo(atomId, assetId, videoId, policy)
          case Left(error) => Left(error)
        }
      } case Left(error) => Left(error)
    }
  }

  private def updateClaim(atomId: String, claimId: String, assetId: String, videoId: String, blockAds: Boolean): Either[VideoUpdateError, String] = {
    MAMLogger.info(s"Updating claim for $videoId", atomId, videoId)
    val policy = getNewPolicy(blockAds)

    val claim = new Claim()
      .setAssetId(assetId)
      .setVideoId(videoId)
      .setPolicy(policy)
      .setContentType("audiovisual")

    try {
      val request = partnerClient.claims
        .patch(claimId, claim)
        .setOnBehalfOfContentOwner(contentOwner)

      YoutubeRequestLogger.logRequest(YoutubeApiType.PartnerApi, YoutubeRequestType.UpdateVideoClaim)
      request.execute()

      MAMLogger.info(s"Successfully updated claim for $videoId, setting blockAds to $blockAds", atomId, videoId)
      Right(s"Updated claim for claim=$claimId asset=$assetId")

    } catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        MAMLogger.error(s"Failed to update claim. ${error.toString} ${error.getMessage}", atomId, videoId)
        Left(VideoUpdateError(s"Error in claiming a video: ${error.toString()}", Some(error.getMessage)))
      }
    }
  }

  private def getPartnerClaim(videoId: String): Option[ClaimSnippet] = {
    val request = partnerClient
      .claimSearch()
      .list
      .setVideoId(videoId)
      .setOnBehalfOfContentOwner(contentOwner)
      .setIncludeThirdPartyClaims(false)
      .setPartnerUploaded(true)

    YoutubeRequestLogger.logRequest(YoutubeApiType.PartnerApi, YoutubeRequestType.GetVideoClaim)
    val response = request.execute()

    if (response.getPageInfo.getTotalResults == 0) {
      None
    } else {
      response.getItems.asScala.toList.headOption
    }
  }

  private def updateTheVideoAdvertisingOptions(videoId: String, atomId: String, enableMidroll: Boolean): Either[VideoUpdateError, String] = {
    // All possible formats can be found on YouTube developer docs: https://developers.google.com/youtube/partner/docs/v1/videoAdvertisingOptions#properties
    val formats: List[String] = List("standard_instream","trueview_instream","display")

    // see https://developers.google.com/youtube/partner/docs/v1/videoAdvertisingOptions#breakPosition[]
    val breakPositions: List[String] = if(enableMidroll) List("preroll", "midroll") else List("preroll")

    val advertisingOption: VideoAdvertisingOption =
      new VideoAdvertisingOption()
        .setAdFormats(formats)
        .setAutoGeneratedBreaks(true)
        .setBreakPosition(breakPositions)

    try {
      MAMLogger.info(s"About to update video advertising options for ${videoId}",atomId,videoId)
      val request = partnerClient.videoAdvertisingOptions().update(videoId, advertisingOption).setOnBehalfOfContentOwner(contentOwner)
      YoutubeRequestLogger.logRequest(YoutubeApiType.PartnerApi, YoutubeRequestType.UpdateVideoAdvertisingOptions)
      request.execute()
      MAMLogger.info(s"Updated video advertising options for ${videoId}",atomId, videoId)
      Right(s"Updated advertising options on video $videoId")
    } catch {
      case e: GoogleJsonResponseException =>
        val error: GoogleJsonError = e.getDetails
        Left(VideoUpdateError(s"Error in updating the advertising options on video $videoId, ${error.toString}", Some(error.getMessage)))
    }
  }

  def createOrUpdateClaim(atomId: String, videoId: String, adSettings: AdSettings): Either[VideoUpdateError, String] = {
    try {
      if(!adSettings.blockAds) {
        updateTheVideoAdvertisingOptions(videoId, atomId, adSettings.enableMidroll)
      }
      getPartnerClaim(videoId) match {
        case Some(claimSnippet) => {
          val claimId = claimSnippet.getId
          val assetId = claimSnippet.getAssetId
          MAMLogger.info(s"Updating an existing claim for $videoId", atomId, videoId)
          updateClaim(atomId, claimId, assetId, videoId, adSettings.blockAds)
        }
        case None => {
          MAMLogger.info(s"No existing claim found for $videoId", atomId, videoId)
          createVideoClaim(atomId, adSettings.blockAds, videoId)
        }
      }
    }

    catch {
      case e: GoogleJsonResponseException => {
        val error: GoogleJsonError = e.getDetails
        MAMLogger.error(s"Failed to create or claim video $videoId. ${error.toString} ${error.getMessage}", atomId, videoId)
        Left(VideoUpdateError(error.toString, Some(error.getMessage)))
      }
    }
  }

  def getAdvertisingOptions(videoId: String): VideoAdvertisingOption = {
    val request = partnerClient.videoAdvertisingOptions().get(videoId)
    YoutubeRequestLogger.logRequest(YoutubeApiType.PartnerApi, YoutubeRequestType.GetVideoAdvertisingOptions)
    request.execute()
  }
}
