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
    message: 'Could not update a composer video page. Make sure you have permissions to relaunch published content. ',
    error: error,
    receivedAt: Date.now()
  };
}

export function updateVideoPage(video, composerUrl, videoBlock, usages) {
  return dispatch => {
    dispatch(requestVideoPageUpdate());

    return VideosApi.updateCanonicalPages(
      video,
      composerUrl,
      videoBlock,
      usages
    )
      .then(() => dispatch(receiveVideoPageUpdate(video.title)))
      .catch(error => dispatch(errorReceivingVideoPageUpdate(error)));
  };
}
