import {pandaReqwest} from './pandaReqwest';


export default {

  fetchVideos: () => {
    return pandaReqwest({
      url: '/api2/atoms',
      method: 'get'
    });
  },

  fetchVideo: (videoId) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId,
      method: 'get',
      contentType: 'application/json'
    });
  },

  createVideo: (video) => {
    return pandaReqwest({
      url: '/api2/atoms',
      contentType: 'application/json',
      method: 'post',
      data: JSON.stringify(video)
    })
  },

  publishVideo: (videoId) => {
    return pandaReqwest({
      url: '/api2/atom/' + videoId + '/publish',
      method: 'put'
    })
  },

  createAsset: (asset, videoId) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId + '/assets',
      contentType: 'application/json',
      method: 'post',
      data: JSON.stringify(asset)
    })
  },

  revertAsset: (version, videoId) => {
    return pandaReqwest({
      url: '/api/atom/' + videoId + '/revert/' + version,
      method: 'put'
    })
  },

  saveVideo: (videoId, video) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId,
      method: 'put',
      contentType: 'application/json',
      data: JSON.stringify(video)
    })
  },

  fetchAudits: (atomId) => {
    return pandaReqwest({
      url: '/api2/audits/' + atomId,
      method: 'get'
    })
  }
}
