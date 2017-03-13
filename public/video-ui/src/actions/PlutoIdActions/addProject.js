import PlutoVideosApi from '../../services/PlutoVideosApi';

function requestAddProject() {
  return {
    type:       'ADD_PROJECT_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveAddProject(videoId) {
  return {
    type:       'ADD_PROJECT_GET_RECEIVE',
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

export function addProject(videoId, plutoId) {
  return dispatch => {
    dispatch(requestAddProject());
    return PlutoVideosApi.addPlutoProject(videoId, plutoId)
      .then(res => {
        dispatch(receiveAddProject(videoId));
      })
      .catch(error => dispatch(errorReceivingAddProject(error)));
  };
}
