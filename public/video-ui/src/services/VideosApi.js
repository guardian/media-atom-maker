import {pandaReqwest} from './pandaReqwest';
import {getStore} from '../util/storeAccessor';
import {composerSyncFields} from '../constants/composerSyncFields';
import ContentApi from './capi';


export default {

  fetchVideos: (search, limit) => {
    let url = `/api2/atoms?limit=${limit}`;
    if(search) {
      url += `&search=${search}`;
    }

    return pandaReqwest({
      url: url,
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

  deleteVideo: (videoId) => {
    return pandaReqwest({
      url: '/api2/atom/' + videoId,
      method: 'delete',
    });
  },

  updateComposerPage(id, metadata, composerUrlBase, videoBlock, usages) {

    function getComposerUpdateRequests(id, metadata, composerUrlBase, videoBlock, isLive) {

      return composerSyncFields.reduce((promises, field) => {
        promises.push(updateArticleField('preview', field, metadata[field], composerUrlBase, id));
        if (!isLive) {
          promises.push(updateArticleField('live', field, metadata[field], composerUrlBase, id));
        }
        return promises;
      }, []);
    }

    function updateArticleField(stage, field, value, composerUrl, pageId) {

      if (value) {
        return pandaReqwest({
          url: `${composerUrl}/api/content/${pageId}/${stage}/fields/${field}`,
          method: 'put',
          contentType: 'application/json',
          crossOrigin: true,
          withCredentials: true,
          data: JSON.stringify(value)
        });
      }
      return pandaReqwest({
        url: `${composerUrl}/api/content/${pageId}/${stage}/fields/${field}`,
        method: 'delete',
        contentType: 'application/json',
        crossOrigin: true,
        withCredentials: true
      });
    }

    return Promise.all(usages.map(usage => ContentApi.getLivePage(usage.id)))
    .then(responses => {
      const promises = responses.map((response, index) => {

        const isLive = response.status == 'ok';

        const fieldPromises = getComposerUpdateRequests(usages[index].fields.internalComposerCode, metadata, composerUrlBase, videoBlock, isLive);

        const videoPagePromise = this.addVideoToComposerPage(usages[index].fields.internalComposerCode, videoBlock, composerUrlBase, isLive);

        return fieldPromises.concat(videoPagePromise);
      });

      return Promise.all([].concat.apply([], promises));
    })
    .catch(error => { console.log('caught error ', error); });
  },

  createComposerPage(id, metadata, composerUrlBase) {

    const initialComposerUrl = composerUrlBase + '/api/content?atomPoweredVideo=true&originatingSystem=composer&type=video';

    const properties = composerSyncFields.reduce((queryStrings, property) => {
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

  addVideoToComposerPage(pageId, previewData, composerUrl, isLive) {

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
      if (!isLive) {
        const liveData = preview.data.block;

        return updateMainBlock('live', liveData);
      }
      return;

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
