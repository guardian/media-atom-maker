import { browserHistory } from 'react-router';
import VideosApi from '../../services/VideosApi';

function requestVideoCreate() {
  return {
    type: 'VIDEO_CREATE_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoCreate(video) {
  browserHistory.push('/videos/' + video.id);
  return {
    type: 'VIDEO_CREATE_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };
}

function errorVideoCreate(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not create video',
    error: error,
    receivedAt: Date.now()
  };
}

export function createVideo(video) {
  return dispatch => {
    dispatch(requestVideoCreate());
    return VideosApi.createVideo(video)
      .then(res => dispatch(receiveVideoCreate(res)))
      .catch(error => dispatch(errorVideoCreate(error)));
  };
}

export function populateEmptyVideo() {
  return {
    type:        'VIDEO_POPULATE_BLANK',
    receivedAt:  Date.now()
  };
}
