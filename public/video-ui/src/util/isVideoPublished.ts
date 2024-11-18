import { Video } from "../services/VideosApi";

export function isVideoPublished(video: Video) {
  if (!video) {
    return false;
  }

  return video.contentChangeDetails && video.contentChangeDetails.published;
}

export function hasVideoExpired(video: Video) {
  if (!video || !video.expiryDate) {
    return false;
  }

  return video.expiryDate < Date.now();
}
