import {pandaReqwest} from './pandaReqwest';

export function getYoutubeCategories() {
  return pandaReqwest({
    url: '/api/youtube/categories',
    contentType: 'application/json',
    method: 'get'
  })
}

export function getYoutubeChannels() {
  return pandaReqwest({
    url: '/api/youtube/channels',
    contentType: 'application/json',
    method: 'get'
  })
}
