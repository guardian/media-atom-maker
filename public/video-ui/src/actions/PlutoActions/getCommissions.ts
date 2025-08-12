import { Dispatch } from 'redux';
import { KnownAction } from '../actions';
import { getPlutoCommissions, PlutoCommission } from '../../services/PlutoApi';

function requestCommissions(): KnownAction {
  return {
    type: 'PLUTO_COMMISSIONS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveCommissions(commissions: PlutoCommission[]): KnownAction {
  return {
    type: 'PLUTO_COMMISSIONS_GET_RECEIVE',
    receivedAt: Date.now(),
    commissions
  };
}

function errorReceivingCommissions(error: unknown): KnownAction {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get Pluto Commissions',
    receivedAt: Date.now(),
    error: error
  };
}

export function getCommissions() {
  return (dispatch: Dispatch<KnownAction>) => {
    dispatch(requestCommissions());
    return getPlutoCommissions()
      .then(commissions => {
        dispatch(receiveCommissions(commissions));
      })
      .catch(err => {
        dispatch(errorReceivingCommissions(err));
      });
  };
}
