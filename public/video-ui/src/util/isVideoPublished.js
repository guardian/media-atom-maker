export function isVideoPublished(video) {
  if (!video) {
    return false;
  }

  return video.contentChangeDetails && video.contentChangeDetails.published;
}

//this will be simplified once we eliminate the expiryDate field
export function hasVideoExpired(video) {
  if (
    !video ||
    (!video.expiryDate &&
      (!video.contentChangeDetails ||
        !video.contentChangeDetails.expiry ||
        !video.contentChangeDetails.expiry.date))
  ) {
    return false;
  }
  return (
    video.contentChangeDetails.expiry.date < Date.now() ||
    video.expiryDate < Date.now()
  );
}
