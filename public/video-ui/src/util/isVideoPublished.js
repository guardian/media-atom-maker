export function isVideoPublished(video) {
  if (!video) {
    return false;
  }

  return video.contentChangeDetails && video.contentChangeDetails.published;
}

export function hasVideoExpired(video) {
  if (!video || !video.expiryDate) {
    return false;
  }

  return video.expiryDate < Date.now();
}
