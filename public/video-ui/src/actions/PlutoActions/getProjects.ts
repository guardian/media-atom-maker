import { Dispatch } from 'redux';
import { KnownAction } from '../actions';
import { getPlutoProjects, PlutoProject } from '../../services/PlutoApi';

function requestProjects(): KnownAction {
  return {
    type: 'PLUTO_PROJECTS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveProjects(plutoProjects: PlutoProject[]): KnownAction {
  return {
    type: 'PLUTO_PROJECTS_GET_RECEIVE',
    receivedAt: Date.now(),
    projects: plutoProjects
  };
}

function errorReceivingProjects(error: unknown): KnownAction {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get Pluto Projects',
    receivedAt: Date.now(),
    error: error
  };
}

export function getProjects(commissionId: string) {
  return (dispatch: Dispatch<KnownAction>) => {
    dispatch(requestProjects());
    return getPlutoProjects({ commissionId })
      .then(plutoProjects => {
        dispatch(receiveProjects(plutoProjects));
      })
      .catch(err => {
        dispatch(errorReceivingProjects(err));
      });
  };
}
