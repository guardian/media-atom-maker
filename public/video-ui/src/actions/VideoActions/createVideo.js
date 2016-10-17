import VideosApi from '../../services/VideosApi';
import { browserHistory } from 'react-router'

export const VIDEO_CREATE_REQUEST = 'VIDEO_CREATE_REQUEST';
export const VIDEO_CREATE_RECEIVE = 'VIDEO_CREATE_RECEIVE';
export const VIDEO_CREATE_ERROR =   'VIDEO_CREATE_ERROR';
export const VIDEO_POPULATE_BLANK = 'VIDEO_POPULATE_BLANK';


function requestVideoCreate() {
  return {
    type:       VIDEO_CREATE_REQUEST,
    receivedAt: Date.now()
  };
}

function recieveVideoCreate(video, refreshSections) {
  browserHistory.push('/video/videos/' + video.id);
  return {
    type: VIDEO_CREATE_RECEIVE,
    video: video,
    refreshSections: refreshSections,
    receivedAt: Date.now()
  };

}

function errorVideoCreate(error) {
  return {
    type:       VIDEO_CREATE_ERROR,
    message:    'Could not create video',
    error:      error,
    receivedAt: Date.now()
  };
}

export function createVideo(video) {
  return dispatch => {
    dispatch(requestVideoCreate());
    return VideosApi.createVideo(video)
        .then(res => dispatch(recieveVideoCreate(res)))
        .fail(error => dispatch(errorVideoCreate(error)));
  };
}
