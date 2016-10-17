import {fetchAtom} from '../../services/AtomsApi';

function requestAtom(id) {
  return {
    type:       'ATOM_GET_REQUEST',
    id:         id,
    receivedAt: Date.now()
  };
}

function recieveAtom(campaign) {
  return {
    type:        'ATOM_GET_RECIEVE',
    atom:    atom,
    receivedAt:  Date.now()
  };
}

function errorRecievingAtom(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get atom',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getAtom(id) {
  return dispatch => {
    dispatch(requestAtom(id));
    return fetchAtom(id)
        .catch(error => dispatch(errorRecievingAtom(error)))
        .then(res => {
          dispatch(recieveAtom(res));
        });
  };
}
