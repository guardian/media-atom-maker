import { apiRequest } from './apiRequest';

export function getYoutubeCategories() {
  return apiRequest({
    url: '/api/youtube/categories'
  });
}

export function getYoutubeChannels() {
  return apiRequest({
    url: '/api/youtube/channels'
  });
}

export function getProcessingStatus(videoIds) {
  return apiRequest({
    url: `/api/youtube/processingStatus?videoIds=${videoIds.join(',')}`
  });
}
