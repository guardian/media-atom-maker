import VideosApi from '../../services/VideosApi';

function requestVideoPageUpdate() {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoPageUpdate(updatedUsages) {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_RECEIVE',
    updatedUsages: updatedUsages,
    receivedAt: Date.now()
  };
}

function errorReceivingVideoPageUpdate(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not update a composer video page. Make sure you have permissions to relaunch published content. ',
    error: error,
    receivedAt: Date.now()
  };
}

export function updateVideoPage(id, video, composerUrl, videoBlock, usages) {
  return dispatch => {
    dispatch(requestVideoPageUpdate());

    return VideosApi.updateComposerPage(
      id,
      video,
      composerUrl,
      videoBlock,
      usages
    )
      .then(updatedUsages => {

        return dispatch(receiveVideoPageUpdate(updatedUsages));
      })
      .catch(error => {
        dispatch(errorReceivingVideoPageUpdate(error));
      });
  };
}
