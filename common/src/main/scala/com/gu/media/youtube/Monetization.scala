package com.gu.media.youtube
import com.google.api.services.youtubePartner.{YouTubePartner, YouTubePartnerRequestInitializer, YouTubePartnerRequest}
//what is key? what is initialise
import com.google.api.services.youtubePartner.YouTubePartner.Builder
import com.google.api.services.youtubePartner.model.Asset




class Monetization(youTube: YouTubeAccess) {



  def createAsset(title: String, description: String, channelId: String) = {
    //TODO
    YouTubePartner
  }

  def setOwnership(assetId: Int) = {

  }

  def claimVideo(assetId: String, videoId: String, policyId: String) = {
    //TODO
  }

}
