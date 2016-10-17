import VideosApi from '../../services/VideosApi';

function requestVideo(id) {
  return {
    type:       'VIDEO_GET_REQUEST',
    id:         id,
    receivedAt: Date.now()
  };
}

function recieveVideo(video) {
  return {
    type:       'VIDEO_GET_RECIEVE',
    video:      video,
    receivedAt: Date.now()
  };
}

function errorRecievingVideo(error) {
  return {
    type:       'VIDEO_ERROR',
    message:    'Could not get video',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getVideo(id) {
  return dispatch => {
    dispatch(requestVideo(id));
    return VideosApi.fetchVideo(id)
        .catch(error => dispatch(errorRecievingVideo(error)))
        .then(res => {
          dispatch(recieveVideo(res));
        });
  };
}
