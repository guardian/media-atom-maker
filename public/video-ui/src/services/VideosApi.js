import {pandaReqwest} from './pandaReqwest';
import {getStore} from '../util/storeAccessor';


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

  revertAsset: (atomId, videoId) => {
    return pandaReqwest({
      url: '/api2/atom/' + atomId + '/asset-active',
      contentType: 'application/json',
      method: 'put',
      data: JSON.stringify({youtubeId: videoId})
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

  getVideoUsages: (videoId) => {
    const capiProxyUrl = getStore().getState().config.capiProxyUrl;
    return pandaReqwest({
      url: capiProxyUrl + "/atom/media/" + videoId + "/usage",
      method: 'get'
    })
  }
}
