import { browserHistory } from 'react-router';
import VideosApi from '../../services/VideosApi';


function requestVideoCreate() {
  return {
    type:       'VIDEO_CREATE_REQUEST',
    receivedAt: Date.now()
  };
}

function recieveVideoCreate(video, refreshSections) {
  browserHistory.push('/video/videos/' + video.id);
  return {
    type: 'VIDEO_CREATE_RECEIVE',
    video: video,
    refreshSections: refreshSections,
    receivedAt: Date.now()
  };

}

function errorVideoCreate(error) {
  return {
    type:       'SHOW_ERROR',
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
