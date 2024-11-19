import VideosApi from '../../services/VideosApi';
import { blankUsageData } from '../../constants/blankUsageData';
import ErrorMessages from '../../constants/ErrorMessages';

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
    message: ErrorMessages.usages,
    error: error,
    receivedAt: Date.now()
  };
}

export function getUsages(id) {
  return dispatch => {
    dispatch(requestVideoUsages());

    if (!id) {
      return dispatch(receiveVideoUsages(blankUsageData));
    }

    return VideosApi.getVideoUsages(id)
      .then(usages => {
        dispatch(receiveVideoUsages(usages));
      })
      .catch(error => {
        console.log({usagesError: error})
        dispatch(errorReceivingVideoUsages(error));
      });
  };
}
