export function isVideoPublished(video) {
  return video && video.contentChangeDetails && video.contentChangeDetails.published;
}
