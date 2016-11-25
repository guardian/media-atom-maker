import { browserHistory } from 'react-router';
import VideosApi from '../../services/VideosApi';

function requestVideoPublish() {
  return {
    type:       'VIDEO_PUBLISH_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoPublish() {
  return {
    type: 'VIDEO_PUBLISH_RECEIVE',
    receivedAt: Date.now()
  };
}

function errorVideoPublish(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not publish video',
    error:      error,
    receivedAt: Date.now()
  };
}

export function publishVideo(video) {
  return dispatch => {
    dispatch(requestVideoPublish());
    return VideosApi.publishVideo(video)
        .then(res => dispatch(receiveVideoPublish(res)))
        .catch(error => dispatch(errorVideoPublish(error)));
  };
}

