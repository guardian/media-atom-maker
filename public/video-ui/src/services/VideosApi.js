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

  createVideoPage: (videoId, title, composerUrl) => {
    return pandaReqwest({
      url: composerUrl + '/api/content?atomPoweredVideo=true&originatingSystem=composer&type=video&initialTitle='+title,
      method: 'post',
      contentType: 'application/json',
      crossOrigin: true,
      withCredentials: true
    });
  },

  addVideoToPage: (pageId, data, composerUrl) => {
    return pandaReqwest({
      url: composerUrl + '/api/content/' + pageId + '/preview/mainblock',
      method: 'post',
      contentType: 'application/json',
      crossOrigin: true,
      withCredentials: true,
      data: JSON.stringify(data)
    });
  }
}
