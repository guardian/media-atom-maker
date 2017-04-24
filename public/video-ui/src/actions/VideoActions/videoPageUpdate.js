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

export function updateVideoPage(id, metadata, composerUrl, videoBlock, usages) {
  return dispatch => {

    dispatch(requestVideoPageUpdate());

    return VideosApi.updateComposerPage(id, metadata, composerUrl, videoBlock, usages)
      .then(res => {
        console.log('res from update ', res);
        dispatch(receiveVideoPageUpdate());
      })
      .catch(error => {
        dispatch(errorReceivingVideoPageUpdate(error));
      });
  };
}
