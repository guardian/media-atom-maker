export function channelAllowed(video, channels) {
  if (!video.channelId) {
    return true;
  }

  if (video.category === 'Hosted') {
    return false;
  }

  if (!channels || channels.length === 0) {
    return false;
  }

  return channels.some(channel => video.channelId === channel.id);
}
