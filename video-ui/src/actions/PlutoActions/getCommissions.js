import { getPlutoCommissions } from '../../services/PlutoApi';

function requestCommissions() {
  return {
    type: 'PLUTO_COMMISSIONS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveCommissions(response) {
  return {
    type: 'PLUTO_COMMISSIONS_GET_RECEIVE',
    receivedAt: Date.now(),
    commissions: response
  };
}

function errorReceivingCommissions(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get Pluto Commissions',
    receivedAt: Date.now(),
    error: error
  };
}

export function getCommissions() {
  return dispatch => {
    dispatch(requestCommissions());
    return getPlutoCommissions()
      .then(res => {
        dispatch(receiveCommissions(res));
      })
      .catch(err => {
        dispatch(errorReceivingCommissions(err));
      });
  };
}
