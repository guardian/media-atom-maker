import VideosApi from '../../services/VideosApi';

function requestAddProject() {
  return {
    type: 'ADD_PROJECT_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveAddProject(video) {
  return {
    type: 'ADD_PROJECT_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };
}

function errorReceivingAddProject(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not add video to pluto project',
    error: error,
    receivedAt: Date.now()
  };
}

export function addProject(video) {
  return dispatch => {
    dispatch(requestAddProject());

    return VideosApi.saveVideo(video.id, video)
      .then(() => dispatch(receiveAddProject(video)))
      .catch(error => dispatch(errorReceivingAddProject(error)));
  };
}
