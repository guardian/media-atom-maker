import VideosApi from '../../services/VideosApi';
import ContentApi from '../../services/capi';

function requestVideoPageUpdate() {
  return {
    type:       'VIDEO_PAGE_UPDATE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoPageUpdate(capiPage) {
  return {
    type:           'VIDEO_PAGE_UPDATE_POST_RECEIVE',
    newPage:        capiPage,
    receivedAt:     Date.now()
  };
}

function errorReceivingVideoPageUpdate(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not update a video page',
    error:      error,
    receivedAt: Date.now()
  };
}

export function updateVideoPage(id, metadata, composerUrl, videoBlock) {
  return dispatch => {

    dispatch(requestVideoPageUpdate());

    return VideosApi.updateComposerPage(id, metadata, composerUrl)
      .then(res => {
        console.log('res from page update is ', res);
        const pageId = res.data.id;
        const pagePath = res.data.identifiers.path.data;

        return VideosApi.addVideoToComposerPage(pageId, videoBlock, composerUrl)
          .then(() => {
            // it takes a little time for the new Composer page to get to CAPI,
            // so keep trying until success or timeout
            ContentApi.getByPath(pagePath, true)
              .then(capiResponse => {
                dispatch(receiveVideoPageUpdate(capiResponse.response.content));
              });
          });
      })
      .catch(error => {
        dispatch(errorReceivingVideoPageUpdate(error));
      });
  };
}
