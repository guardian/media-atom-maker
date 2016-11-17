import VideosApi from '../../services/VideosApi';

function requestVideo(id) {
  return {
    type:       'VIDEO_GET_REQUEST',
    id:         id,
    receivedAt: Date.now()
  };
}

function receiveVideo(video) {
  return {
    type:       'VIDEO_GET_RECEIVE',
    video:      video,
    receivedAt: Date.now()
  };
}

function errorReceivingVideo(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get video',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getVideo(id) {
  return dispatch => {
    dispatch(requestVideo(id));
    return VideosApi.fetchVideo(id)
        .catch(error => dispatch(errorReceivingVideo(error)))
        .then(res => {

          //TODO remove this once the API returns more structured data
          if (! res.data.metadata) {
            res.data.metadata = {};
          }

          dispatch(receiveVideo(res));
        });
  };
}
