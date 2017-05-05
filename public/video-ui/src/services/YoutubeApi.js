import {pandaReqwest} from './pandaReqwest';

export function getYoutubeCategories() {
  return pandaReqwest({
    url: '/api/youtube/categories'
  });
}

export function getYoutubeChannels() {
  return pandaReqwest({
    url: '/api/youtube/channels'
  });
}

export function getProcessingStatus(videoIds) {
  return pandaReqwest({
    url: `/api/youtube/processingStatus?videoIds=${videoIds.join(',')}`
  });
}
