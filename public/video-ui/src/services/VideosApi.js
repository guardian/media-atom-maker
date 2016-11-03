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

  publishVideo: (videoId) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId + '/publish',
      method: 'post'
    })
  },

  createAsset: (asset, videoId) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId + '/asset',
      method: 'post',
      data: asset
    })
  },

  saveVideo: (videoId, video) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId,
      method: 'post',
      data: video.data
    })
  }

}