export function isVideoPublished(video) {
  if (!video) {
    return false;
  }
  return Object.keys(video).length !== 0;
}

export function hasVideoExpired(video) {
  if (!video || !video.expiryDate) {
    return false;
  }

  return video.expiryDate < Date.now(); 
}
