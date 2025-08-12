import { apiRequest } from './apiRequest';

export type YouTubeVideoCategory = {
  id: number;
  title: string
};

type YouTubePrivacyStatus = "Private" | "Unlisted" | "Public"

export type YouTubeChannelWithData = {
  id: string;
  title: string;
  privacyStates: YouTubePrivacyStatus[],
  isCommercial: boolean
}

export function getYoutubeCategories() {
  return apiRequest<YouTubeVideoCategory[]>({
    url: '/api/youtube/categories'
  });
}

export function getYoutubeChannels() {
  return apiRequest<YouTubeChannelWithData[]>({
    url: '/api/youtube/channels'
  });
}
