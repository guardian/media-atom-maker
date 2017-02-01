export function isVideoPublished(video) {
  if (!video) {
    return false;
  }
  return Object.keys(video).length !== 0;
}
