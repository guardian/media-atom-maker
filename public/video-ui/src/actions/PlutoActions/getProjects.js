import { getPlutoProjects } from '../../services/PlutoApi';

function requestProjects() {
  return {
    type: 'PLUTO_PROJECTS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveProjects(response) {
  return {
    type: 'PLUTO_PROJECTS_GET_RECEIVE',
    receivedAt: Date.now(),
    projects: response
  };
}

function errorReceivingProjects(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get Pluto Projects',
    receivedAt: Date.now(),
    error: error
  };
}

export function getProjects() {
  return dispatch => {
    dispatch(requestProjects());
    return getPlutoProjects()
      .then(res => {
        dispatch(receiveProjects(res));
      })
      .catch(err => {
        dispatch(errorReceivingProjects(err));
      });
  };
}
