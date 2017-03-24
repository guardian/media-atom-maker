import PlutoVideosApi from '../../services/PlutoVideosApi';

function requestAddProject() {
  return {
    type:       'ADD_PROJECT_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveAddProject(videoId) {
  return {
    type:       'ADD_PROJECT_RECEIVE',
    videoId:    videoId,
    receivedAt: Date.now()
  };
}

function errorReceivingAddProject(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not add video to pluto project',
    error:      error,
    receivedAt: Date.now()
  };
}

export function addProject(video) {
  return dispatch => {
    dispatch(requestAddProject());
    return PlutoVideosApi.sendToPluto(video.id, video.plutoProjectId)
      .then(() => {
        dispatch(receiveAddProject(video.id));
      })
      .catch(error => dispatch(errorReceivingAddProject(error)));
  };
}
