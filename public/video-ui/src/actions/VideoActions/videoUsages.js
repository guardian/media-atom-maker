import VideosApi from '../../services/VideosApi';

function requestVideoUsages() {
  return {
    type:       'VIDEO_USAGE_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoUsages(usages) {
  return {
    type:           'VIDEO_USAGE_GET_RECEIVE',
    usages:         usages,
    receivedAt:     Date.now()
  };
}

function errorReceivingVideoUsages(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get video usages',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getUsages(id) {
  return dispatch => {
    dispatch(requestVideoUsages());
    return VideosApi.getVideoUsages(id)
    .then(res => {
      const usages = res.response.results;
      dispatch(receiveVideoUsages(usages));
    })
    .catch(error => {
      dispatch(errorReceivingVideoUsages(error));
    });
  };
}
