import VideosApi from '../../services/VideosApi';
import ContentApi from '../../services/capi';

function requestVideoUsages() {
  return {
    type: 'VIDEO_USAGE_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoUsages(usages) {
  return {
    type: 'VIDEO_USAGE_GET_RECEIVE',
    usages: usages,
    receivedAt: Date.now()
  };
}

function errorReceivingVideoUsages(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get video usages',
    error: error,
    receivedAt: Date.now()
  };
}

export function getUsages(id) {
  return dispatch => {
    dispatch(requestVideoUsages());

    if (!id) {
      return dispatch(receiveVideoUsages({}));
    }

    return VideosApi.getVideoUsages(id)
      .then(usages => {
        dispatch(receiveVideoUsages(usages));
      })
      .catch(error => {
        dispatch(errorReceivingVideoUsages(error));
      });
  };
}
