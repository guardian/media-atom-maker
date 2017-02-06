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

  fetchPublishedVideo: (videoId) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId + '/published',
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
    });
  },

  publishVideo: (videoId) => {
    return pandaReqwest({
      url: '/api2/atom/' + videoId + '/publish',
      method: 'put'
    });
  },

  createAsset: (asset, videoId) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId + '/assets',
      contentType: 'application/json',
      method: 'post',
      data: JSON.stringify(asset)
    });
  },

  revertAsset: (atomId, videoId) => {
    return pandaReqwest({
      url: '/api2/atom/' + atomId + '/asset-active',
      contentType: 'application/json',
      method: 'put',
      data: JSON.stringify({youtubeId: videoId})
    });
  },

  saveVideo: (videoId, video) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId,
      method: 'put',
      contentType: 'application/json',
      data: JSON.stringify(video)
    });
  },

  fetchAudits: (atomId) => {
    return pandaReqwest({
      url: '/api2/audits/' + atomId,
      method: 'get'
    });
  },

  getVideoUsages: (videoId) => {
    const capiProxyUrl = getStore().getState().config.capiProxyUrl;
    return pandaReqwest({
      url: capiProxyUrl + "/atom/media/" + videoId + "/usage",
      method: 'get'
    });
  },

  createComposerPage(id, metadata, composerUrlBase) {

    const initialComposerUrl = composerUrlBase + '/api/content?atomPoweredVideo=true&originatingSystem=composer&type=video';
    const propertiesToSend= ['title', 'standfirst'];

    const properties = propertiesToSend.reduce((queryStrings, property) => {
      if (metadata[property]) {
        queryStrings.push('&initial' + property.charAt(0).toUpperCase() + property.slice(1) + '=' + metadata[property]);
      }
      return queryStrings;
    }, []);
    const composerUrl = initialComposerUrl + properties.join('');

    return pandaReqwest({
      url: composerUrl,
      method: 'post',
      contentType: 'application/json',
      crossOrigin: true,
      withCredentials: true
    });
  },

  addVideoToComposerPage(pageId, previewData, composerUrl) {

    function updateMainBlock(stage, data) {
      return pandaReqwest({
        url: `${composerUrl}/api/content/${pageId}/${stage}/mainblock`,
        method: 'post',
        contentType: 'application/json',
        crossOrigin: true,
        withCredentials: true,
        data: JSON.stringify(data)
      });
    }

    // The composer client (whilst in draft) keeps both the preview and live data in sync so we must do the same
    return updateMainBlock('preview', previewData).then((preview) => {
      const liveData = preview.data.block;
      return updateMainBlock('live', liveData);
    });
  },

  fetchComposerId(capiId) {
    const capiProxyUrl = getStore().getState().config.capiProxyUrl;
    return pandaReqwest({
      url: capiProxyUrl + '/' + capiId + "?show-fields=all",
      method: 'get'
    })
    .then(resp => {
      if (resp.response.content && resp.response.content.fields && resp.response.content.fields.internalComposerCode) {
        return resp.response.content.fields.internalComposerCode;
      }
      return "";
  });
}

};
