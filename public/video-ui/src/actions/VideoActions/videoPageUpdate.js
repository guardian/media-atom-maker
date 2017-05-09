import VideosApi from '../../services/VideosApi';

function requestVideoPageUpdate() {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoPageUpdate(newTitle) {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_RECEIVE',
    newTitle: newTitle,
    receivedAt: Date.now()
  };
}

function errorReceivingVideoPageUpdate(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not update a video page',
    error: error,
    receivedAt: Date.now()
  };
}

export function updateVideoPage(id, metadata, composerUrl, videoBlock, usages) {
  return dispatch => {
    dispatch(requestVideoPageUpdate());

    return VideosApi.updateComposerPage(
      id,
      metadata,
      composerUrl,
      videoBlock,
      usages
    )
      .then(() => {
        return dispatch(receiveVideoPageUpdate(metadata.headline));
      })
      .catch(error => {
        dispatch(errorReceivingVideoPageUpdate(error));
      });
  };
}
