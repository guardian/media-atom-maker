import {fetchAtoms} from '../../services/AtomsApi';

function requestAtoms() {
  return {
    type:       'ATOMS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function recieveAtoms(atoms) {
  return {
    type:        'ATOMS_GET_RECIEVE',
    atoms:   atoms,
    receivedAt:  Date.now()
  };
}

function errorRecievingAtoms(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get atoms',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getAtoms() {
  return dispatch => {
    dispatch(requestAtoms());
    return fetchAtoms()
        .catch(error => dispatch(errorRecievingAtoms(error)))
        .then(res => {
          dispatch(recieveAtoms(res));
        });
  };
}
