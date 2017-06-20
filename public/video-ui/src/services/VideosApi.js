import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';
import { getComposerData, getRightsPayload } from '../util/getComposerData';
import ContentApi from './capi';

export default {
  fetchVideos: (search, limit) => {
    let url = `/api2/atoms?limit=${limit}`;
    if (search) {
      url += `&search=${search}`;
    }

    return pandaReqwest({
      url: url
    });
  },

  fetchVideo: videoId => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId
    });
  },

  fetchPublishedVideo: videoId => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId + '/published'
    });
  },

  createVideo: video => {
    return pandaReqwest({
      url: '/api2/atoms',
      method: 'post',
      data: video
    });
  },

  publishVideo: videoId => {
    return pandaReqwest({
      url: '/api2/atom/' + videoId + '/publish',
      method: 'put'
    });
  },

  createAsset: (asset, videoId) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId + '/assets',
      method: 'post',
      data: asset
    });
  },

  revertAsset: (atomId, videoId) => {
    return pandaReqwest({
      url: '/api2/atom/' + atomId + '/asset-active',
      method: 'put',
      data: { youtubeId: videoId }
    });
  },

  saveVideo: (videoId, video) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId,
      method: 'put',
      data: video
    });
  },

  fetchAudits: atomId => {
    return pandaReqwest({
      url: '/api2/audits/' + atomId
    });
  },

  getVideoUsages: videoId => {
    const capiProxyUrl = getStore().getState().config.capiProxyUrl;
    return pandaReqwest({
      url: capiProxyUrl + '/atom/media/' + videoId + '/usage'
    });
  },

  deleteVideo: videoId => {
    return pandaReqwest({
      url: '/api2/atom/' + videoId,
      method: 'delete'
    });
  },

  updateComposerPage(id, video, composerUrlBase, videoBlock, usages) {
    function getComposerUpdateRequests(
      id,
      video,
      composerUrlBase,
      videoBlock,
      isLive
    ) {
      const rightsExpiryPayload = getRightsPayload(video);
      const rightsRequest = pandaReqwest({
        url: `${composerUrlBase}/api/content/${id}/expiry/rights`,
        method: 'post',
        crossOrigin: true,
        withCredentials: true,
        data: rightsExpiryPayload
      });

      // When article is in preview, composer keeps track of both the live and preview versions of an article
      // For an uplublished article, we need to update both live and preview versions.
      const composerData = getComposerData(video);
      const dataUpdatePromises = composerData.reduce((promises, data) => {
        promises.push(updateArticleField('preview', data, composerUrlBase, id));

        if (!isLive) {
          promises.push(updateArticleField('live', data, composerUrlBase, id));
        }
        return promises;
      }, []);

      dataUpdatePromises.push(rightsRequest);
      return dataUpdatePromises;
    }

    function updateArticleField(stage, data, composerUrl, pageId) {
      if (data.value || data.belongsTo === 'settings') {
        const value = data.isFreeText
          ? data.value.split('"').join('\\"')
          : data.value;
        return pandaReqwest({
          url: `${composerUrl}/api/content/${pageId}/${stage}/${data.belongsTo}/${data.name}`,
          method: 'put',
          crossOrigin: true,
          withCredentials: true,
          data: `"${value}"`
        });
      }

      return pandaReqwest({
        url: `${composerUrl}/api/content/${pageId}/${stage}/${data.belongsTo}/${data.name}`,
        method: 'delete',
        crossOrigin: true,
        withCredentials: true
      });
    }

    return Promise.all(
      usages.map(usage => ContentApi.getLivePage(usage.id))
    ).then(responses => {
      const promises = responses.map((response, index) => {
        const isLive = response.response.status === 'ok';

        const fieldPromises = getComposerUpdateRequests(
          usages[index].fields.internalComposerCode,
          video,
          composerUrlBase,
          videoBlock,
          isLive
        );

        const videoPagePromise = this.addVideoToComposerPage(
          usages[index].fields.internalComposerCode,
          videoBlock,
          composerUrlBase,
          isLive
        );

        return fieldPromises.concat(videoPagePromise);
      });

      return Promise.all([].concat.apply([], promises));
    });
  },

  createComposerPage(id, video, composerUrlBase) {
    const composerData = getComposerData(video);

    const initialComposerUrl =
      composerUrlBase +
      '/api/content?atomPoweredVideo=true&originatingSystem=MediaAtomMaker&type=video';

    const properties = composerData.reduce((queryStrings, data) => {
      if (data.value) {
        const value = data.isFreeText
          ? data.value.split('"').join('\\"')
          : data.value;

        queryStrings.push(
          '&initialVideo' +
            data.name.charAt(0).toUpperCase() +
            data.name.slice(1) +
            '=' +
            value
        );
      }
      return queryStrings;
    }, []);

    let composerUrl = initialComposerUrl + properties.join('');

    if (video.expiryDate) {
      composerUrl += '&initialExpiry=' + video.expiryDate;
    }

    return pandaReqwest({
      url: composerUrl,
      method: 'post',
      crossOrigin: true,
      withCredentials: true
    });
  },

  addVideoToComposerPage(pageId, previewData, composerUrl, isLive) {
    function updateMainBlock(stage, data) {
      return pandaReqwest({
        url: `${composerUrl}/api/content/${pageId}/${stage}/mainblock`,
        method: 'post',
        crossOrigin: true,
        withCredentials: true,
        data: data
      });
    }

    // The composer client (whilst in draft) keeps both the preview and live data in sync so we must do the same

    return updateMainBlock('preview', previewData).then(preview => {
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
      url: capiProxyUrl + '/' + capiId + '?show-fields=all'
    }).then(resp => {
      if (
        resp.response.content &&
        resp.response.content.fields &&
        resp.response.content.fields.internalComposerCode
      ) {
        return resp.response.content.fields.internalComposerCode;
      }
      return '';
    });
  }
};
