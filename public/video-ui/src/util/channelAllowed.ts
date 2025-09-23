import { Video } from "../services/VideosApi";

export type Channel = {
  id: string,
  privacyState: string[]
}

export const channelAllowed = (video: Video, channels: Channel[]) => {
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
};
