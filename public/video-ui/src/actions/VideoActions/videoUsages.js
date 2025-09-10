import VideosApi from '../../services/VideosApi';
import { blankUsageData } from '../../constants/blankUsageData';
import ErrorMessages from '../../constants/ErrorMessages';
import { setFetchingUsage } from '../../slices/saveState';

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
    dispatch(setFetchingUsage(true));

    if (!id) {
      return dispatch(receiveVideoUsages(blankUsageData));
    }

    return VideosApi.getVideoUsages(id)
      .then(usages => {
        dispatch(setFetchingUsage(false));
        dispatch(receiveVideoUsages(usages));
      })
      .catch(error => {
        dispatch(errorReceivingVideoUsages(error));
      });
  };
}
