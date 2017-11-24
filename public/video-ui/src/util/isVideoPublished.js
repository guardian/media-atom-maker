export function isVideoPublished(video) {
  if (!video) {
    return false;
  }

  return video.contentChangeDetails && video.contentChangeDetails.published;
}

export function hasVideoExpired(video) {
  if (
    !video ||
    !video.contentChangeDetails ||
    !video.contentChangeDetails.expiry ||
    !video.contentChangeDetails.expiry.date
  ) {
    return false;
  }

  return video.contentChangeDetails.expiry.date < Date.now();
}
