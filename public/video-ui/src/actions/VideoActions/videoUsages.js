import VideosApi from '../../services/VideosApi';

function requestVideoUsages(id) {
  return {
    type:       'VIDEO_USAGE_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoUsages(usages, id) {
  const usageObject = {};
  usageObject[id] = usages.response.results;
  return {
    type:           'VIDEO_USAGE_GET_RECEIVE',
    usages:         usageObject,
    receivedAt:     Date.now()
  };
}

function errorReceivingVideoUsages(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not create a video page',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getUsages(id) {
  return dispatch => {
    dispatch(requestVideoUsages());
    return VideosApi.getVideoUsages(id)
    .then(res => {
      dispatch(receiveVideoUsages(res, id))
    })
    .catch(error => {
      dispatch(errorReceivingVideoUsages(error))
    })
  };
}
