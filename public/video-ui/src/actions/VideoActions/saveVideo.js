import VideosApi from '../../services/VideosApi';

function requestVideoSave() {
  return {
    type:       'VIDEO_SAVE_REQUEST',
    receivedAt: Date.now()
  };
}

function recieveVideoSave(video) {
  return {
    type:       'VIDEO_SAVE_RECEIVE',
    video:      video,
    receivedAt: Date.now()
  };
}

function errorVideoSave(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not save video',
    error:      error,
    receivedAt: Date.now()
  };
}

export function saveVideo(video) {
  return dispatch => {
    dispatch(requestVideoSave());
    return VideosApi.saveVideo(video.id, video)
        .then(res => dispatch(recieveVideoSave(res)))
        .fail(error => dispatch(errorVideoSave(error)));
  };
}
