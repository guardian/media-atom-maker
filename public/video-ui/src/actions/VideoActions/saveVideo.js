import VideosApi from '../../services/VideosApi';

function requestVideoSave(video) {
  return {
    type:       'VIDEO_SAVE_REQUEST',
    video:      video,
    receivedAt: Date.now()
  };
}

function receiveVideoSave(video) {
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
    dispatch(requestVideoSave(video));
    return VideosApi.saveVideo(video.id, video)
        .then(res => dispatch(receiveVideoSave(res)))
        .catch(error => dispatch(errorVideoSave(error)));
  };
}

