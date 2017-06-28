export function channelAllowed(video, channels) {
  if (video.category === 'Hosted') {
    return false;
  }

  if (!video.channelId || !channels || channels.length === 0) {
    return false;
  }

  return channels.some(channel => video.channelId === channel.id);
}
