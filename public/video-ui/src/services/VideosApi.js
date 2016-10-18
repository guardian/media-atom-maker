import {pandaReqwest} from './pandaReqwest';

export function fetchVideos() {
  return pandaReqwest({
    url: '/api/atoms'
  });
}

export function fetchVideo(videoId) {
  return pandaReqwest({
    url: '/api/atom/' + videoId
  });
}