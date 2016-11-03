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

  createAsset: (asset, videoId) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId + '/asset',
      method: 'post',
      data: asset
    })
  },

  revertAsset: (version, videoId) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId + '/revert/' + version,
      method: 'post'
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