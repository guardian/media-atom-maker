import {pandaReqwest} from './pandaReqwest';


export default {

  fetchVideos: () => {
    return pandaReqwest({
      url: '/api/atoms',
      contentType: 'application/json'
    });
  },

  fetchVideo: (videoId) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId,
      contentType: 'application/json'
    });
  },

  createVideo: (video) => {
    return pandaReqwest({
      url: '/api/atom',
      method: 'post',
      data: video.data
    })
  },

  saveVideo: (videoId, video) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId,
      method: 'put',
      data: video.data
    })
  }

}
