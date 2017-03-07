import PlutoVideosApi from '../../services/PlutoVideosApi';

function requestAddProject() {
  return {
    type:       'ADD_PROJECT_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveAddProject(videos) {
  return {
    type:       'ADD_PROJECT_GET_RECEIVE',
    videos:     videos,
    receivedAt: Date.now()
  };
}

function errorReceivingAddProject(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get videos without pluto ids',
    error:      error,
    receivedAt: Date.now()
  };
}

export function addProject() {
  return dispatch => {
    dispatch(requestAddProject());
    return PlutoVideosApi.addPlutoProject()
      .then(res => {
        dispatch(receiveAddProject(res));
      })
      .catch(error => dispatch(errorReceivingAddProject(error)));
  };
}
